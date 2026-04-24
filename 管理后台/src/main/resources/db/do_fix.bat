@echo off
chcp 65001
set MYSQL_EXE=D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe
set SQL_FILE=f:\AiCoding\traeWork\smallTweaksScm\管理后台\src\main\resources\db\final_fix.sql
set DB_NAME=small_tweaks_scm

echo Executing SQL file: %SQL_FILE%
echo.

"%MYSQL_EXE%" -u root --default-character-set=utf8mb4 %DB_NAME% < "%SQL_FILE%"

echo.
echo Done!
