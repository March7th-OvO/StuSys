"""Keep SQLFluff formatting, then align CREATE TABLE fields to fixed columns.

Offsets are measured from the start of a line: name=4, type=28, COMMENT=70.
If preceding content exceeds a target, preserve it and insert one space.
The VS Code extension uses `fix --stdin-filename ... -`; other CLI commands
are passed through unchanged. Requires SQLFluff in the selected Python.
"""

from __future__ import annotations

import subprocess
import sys
from importlib.metadata import version

from sqlfluff.core import FluffConfig, Linter


NAME_OFFSET = 4
TYPE_OFFSET = 28
COMMENT_OFFSET = 70


def compatible_arguments(arguments: list[str]) -> list[str]:
    """Remove obsolete fix flags if the editor has cached an older CLI version."""
    if (
        arguments
        and arguments[0] == "fix"
        and int(version("sqlfluff").split(".", 1)[0]) >= 3
    ):
        # SQLFluff 3+ always applies fixes. Preserve positional paths after --.
        boundary = arguments.index("--") if "--" in arguments else len(arguments)
        return [
            argument
            for argument in arguments[:boundary]
            if argument not in ("-f", "--force")
        ] + arguments[boundary:]
    return arguments


def apply_edits(text: str, edits: list[tuple[int, int, str]]) -> str:
    """Apply source-coordinate whitespace edits without moving later offsets."""
    for start, end, replacement in sorted(edits, reverse=True):
        text = text[:start] + replacement + text[end:]
    return text


def format_columns(sql: str, filename: str) -> str:
    """Edit whitespace around parsed fields, never identifiers or SQL literals."""
    original_sql = sql
    line_ending = "\r\n" if "\r\n" in sql else "\n"
    # SQLFluff normalizes CRLF before parsing. Match its source coordinates
    # while editing, then restore the buffer's original line ending.
    sql = sql.replace("\r\n", "\n")
    parsed = Linter(config=FluffConfig.from_root()).parse_string(sql, fname=filename)
    # Keep SQLFluff's safety behavior for SQL it cannot fully parse.
    if parsed.violations or parsed.tree is None:
        return original_sql

    edits: list[tuple[int, int, str]] = []
    for table in parsed.tree.recursive_crawl("create_table_statement"):
        for column in table.recursive_crawl("column_definition"):
            parts = [part for part in column.segments if part.is_code]
            if len(parts) < 2 or not parts[0].is_type(
                "naked_identifier", "quoted_identifier"
            ):
                continue
            name = parts[0]
            data_type = next((p for p in parts[1:] if p.is_type("data_type")), None)
            if data_type is None or any(
                p.pos_marker is None or not p.pos_marker.is_literal()
                for p in (name, data_type)
            ):
                continue

            name_span = name.pos_marker.source_slice
            type_span = data_type.pos_marker.source_slice
            line_start = sql.rfind("\n", 0, name_span.start) + 1
            # Inline definitions and intervening comments need their own layout.
            # Leave them intact rather than removing SQL or comments to align them.
            if sql[line_start:name_span.start].strip() or not sql[
                name_span.stop:type_span.start
            ].isspace():
                continue

            column_edits = [
                (line_start, name_span.start, " " * NAME_OFFSET),
                (
                    name_span.stop,
                    type_span.start,
                    " " * max(1, TYPE_OFFSET - NAME_OFFSET - len(name.raw)),
                ),
            ]

            for index, part in enumerate(parts[1:], start=1):
                first_token = next((s for s in part.raw_segments if s.is_code), None)
                if (
                    not part.is_type("column_constraint_segment")
                    or first_token is None
                    or first_token.raw_upper != "COMMENT"
                ):
                    continue
                previous = parts[index - 1]
                if any(
                    p.pos_marker is None or not p.pos_marker.is_literal()
                    for p in (previous, part)
                ):
                    continue
                gap_start = previous.pos_marker.source_slice.stop
                gap_end = part.pos_marker.source_slice.start
                if not sql[gap_start:gap_end].isspace():
                    continue

                # Include the new name/type padding when measuring COMMENT's
                # target. Long types or constraints continue on the same line.
                prefix = apply_edits(
                    sql[line_start:gap_start],
                    [
                        (start - line_start, end - line_start, replacement)
                        for start, end, replacement in column_edits
                    ],
                )
                prefix_width = len(prefix.rsplit("\n", 1)[-1])
                column_edits.append(
                    (gap_start, gap_end, " " * max(1, COMMENT_OFFSET - prefix_width))
                )
            edits.extend(column_edits)

    return apply_edits(sql, edits).replace("\n", line_ending)


def main() -> int:
    """Delegate CLI behavior, adding fixed columns only to editor stdin fixes."""
    arguments = compatible_arguments(sys.argv[1:])
    command = [sys.executable, "-m", "sqlfluff", *arguments]
    if not arguments or arguments[0] != "fix" or "-" not in arguments:
        return subprocess.run(command).returncode

    result = subprocess.run(command, input=sys.stdin.buffer.read(), capture_output=True)
    output = result.stdout
    if result.returncode in (0, 1) and output:
        filename = "stdin.sql"
        if "--stdin-filename" in arguments:
            index = arguments.index("--stdin-filename")
            if index + 1 < len(arguments):
                filename = arguments[index + 1]
        try:
            output = format_columns(output.decode("utf-8"), filename).encode("utf-8")
        except Exception as error:
            # Return the original output with a failure code, so the extension
            # does not replace the buffer with a partially formatted result.
            sys.stdout.buffer.write(result.stdout)
            sys.stderr.buffer.write(result.stderr)
            print(f"Fixed-column formatting failed: {error}", file=sys.stderr)
            return 2
    sys.stdout.buffer.write(output)
    sys.stderr.buffer.write(result.stderr)
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
