# SQL 固定列格式化

项目通过 `sqlfluff-fixed-columns.cmd` 调用已安装 Python 中的 SQLFluff，
再调整独占一行的 `CREATE TABLE` 字段定义。VS Code/Trae 的 SQLFluff 扩展
使用此入口，按 **Shift + Alt + F** 即可运行。

- 字段名前有 4 个空格，从第 5 列开始。
- 数据类型通常从第 29 列开始，行首偏移为 28。
- `COMMENT` 通常从第 71 列开始，行首偏移为 70。
- 前面的内容超过目标位置时，保留一个空格后向右顺延。

固定位置定义在 `sqlfluff_fixed_columns.py` 的三个 `*_OFFSET` 常量中。
脚本先运行 SQLFluff，再基于解析树调整空白，保留 SQL 字面量和注释内容。
无法解析的 SQL 和单行紧凑表定义不会执行固定列调整。

依赖：Windows 的 `py -3` 能找到已安装 SQLFluff 的 Python。
工作区 `.vscode/settings.json` 将 `sqlfluff.executablePath` 指向此批处理入口，
并启用 `sqlfluff.shell`。`.sqlfluff` 中类型和字段约束的 `spacing_before = any`
用于保留固定列填充，其余检查继续生效。

本入口对扩展使用的 `fix --stdin-filename 文件名 -` 追加固定列处理；
其它 SQLFluff 命令直接交给原始 CLI。
如果扩展缓存了旧版 CLI 信息而自动传入 `-f` / `--force`，入口会在
SQLFluff 3.0 及以上版本中移除这些已废弃的选项，避免弹出弃用提示。

在项目根目录运行回归检查：

```powershell
py -3 tools/test_sqlfluff_fixed_columns.py
```
