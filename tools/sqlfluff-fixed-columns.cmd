@echo off
rem Use the installed Python and keep all arguments from the SQLFluff extension.
py -3 "%~dp0sqlfluff_fixed_columns.py" %*
exit /b %errorlevel%
