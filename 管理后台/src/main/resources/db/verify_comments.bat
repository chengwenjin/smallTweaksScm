@echo off
chcp 65001 >nul
set MYSQL_EXE=D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe
set DB_NAME=small_tweaks_scm

echo.
echo ========== 表注释 ==========
echo.

%MYSQL_EXE% -u root --default-character-set=utf8mb4 %DB_NAME% -e "SELECT TABLE_NAME AS '表名', TABLE_COMMENT AS '表注释' FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME LIKE 'scm_%%' ORDER BY TABLE_NAME;"

echo.
echo ========== 字段注释 ==========
echo.

%MYSQL_EXE% -u root --default-character-set=utf8mb4 %DB_NAME% -e "SELECT TABLE_NAME AS '表名', COLUMN_NAME AS '字段名', COLUMN_COMMENT AS '字段注释' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME LIKE 'scm_%%' ORDER BY TABLE_NAME, ORDINAL_POSITION;"

echo.
echo ========== 验证完成 ==========
pause
