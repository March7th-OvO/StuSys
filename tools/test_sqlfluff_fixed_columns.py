"""Regression checks for fixed columns, overflow and SQL content preservation."""

import unittest
from unittest.mock import patch

from sqlfluff.core import FluffConfig, Linter

from sqlfluff_fixed_columns import compatible_arguments, format_columns


class FixedColumnsTests(unittest.TestCase):
    def setUp(self):
        self.linter = Linter(config=FluffConfig.from_root())

    def code_tokens(self, sql):
        tree = self.linter.parse_string(sql).tree
        return [segment.raw for segment in tree.raw_segments if segment.is_code]

    def test_deprecated_force_flags_follow_the_installed_cli_version(self):
        arguments = ["fix", "--force", "-f", "--stdin-filename", "example.sql", "-"]
        with patch("sqlfluff_fixed_columns.version", return_value="4.3.0"):
            self.assertEqual(
                compatible_arguments(arguments),
                ["fix", "--stdin-filename", "example.sql", "-"],
            )
            self.assertEqual(
                compatible_arguments(["fix", "--", "--force"]),
                ["fix", "--", "--force"],
            )
            self.assertEqual(compatible_arguments(["--version"]), ["--version"])
        with patch("sqlfluff_fixed_columns.version", return_value="2.3.0"):
            self.assertEqual(compatible_arguments(arguments), arguments)

    def test_fixed_columns_overflow_and_line_endings(self):
        source = (
            "CREATE TABLE example (\n"
            "    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT 'ID',\n"
            "    name VARCHAR(20) NOT NULL COMMENT 'a  b',\n"
            "    notes_text TEXT COMMENT 'it''s  intact',\n"
            "    long_text VARCHAR(100) DEFAULT 'a long default value exceeding the target' "
            "COMMENT 'overflow'\n"
            ");\n"
        )
        for ending in ("\n", "\r\n"):
            with self.subTest(ending=repr(ending)):
                original = source.replace("\n", ending)
                fixed = format_columns(original, "example.sql")
                lines = fixed.splitlines()
                self.assertTrue(lines[1].startswith("    id"))
                self.assertEqual(lines[1].index("BIGINT"), 28)
                self.assertEqual(lines[2].index("VARCHAR"), 28)
                self.assertEqual(lines[3].index("TEXT"), 28)
                self.assertEqual(lines[1].index("COMMENT"), 70)
                self.assertEqual(lines[2].index("COMMENT"), 70)
                self.assertEqual(lines[3].index("COMMENT"), 70)
                # A long DEFAULT clause still continues after a single space.
                self.assertIn("target' COMMENT", lines[4])
                self.assertGreater(lines[4].index("COMMENT"), 70)
                self.assertEqual(format_columns(fixed, "example.sql"), fixed)
                self.assertEqual(self.code_tokens(original), self.code_tokens(fixed))
                self.assertFalse(self.linter.lint_string(fixed).get_violations())
                self.assertEqual(fixed.count(ending), len(lines))

    def test_quoted_identifiers_and_literals_are_preserved(self):
        source = (
            "CREATE TABLE example (\n"
            "    `long field name` VARCHAR(20) DEFAULT 'a  b' "
            "COMMENT 'it''s  intact'\n"
            ");\n"
        )
        fixed = format_columns(source, "example.sql")
        self.assertEqual(self.code_tokens(source), self.code_tokens(fixed))
        self.assertEqual(format_columns(fixed, "example.sql"), fixed)

    def test_queries_inline_definitions_and_parse_errors_are_preserved(self):
        for source in (
            "SELECT 'COMMENT name';\n",
            "CREATE TABLE example (id BIGINT COMMENT 'example');\n",
            "CREATE TABLE example (\n    id BIGINT COMMENT 'unterminated\n",
        ):
            with self.subTest(source=source):
                self.assertEqual(format_columns(source, "example.sql"), source)


if __name__ == "__main__":
    unittest.main()
