@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set MYSQL_EXE=D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe
set DB_NAME=small_tweaks_scm
set SQL_FILE=f:\AiCoding\traeWork\smallTweaksScm\管理后台\src\main\resources\db\fix_comments_final.sql

echo ============================================================
echo   修复SCM数据库中文注释乱码
echo ============================================================
echo.
echo MySQL客户端: %MYSQL_EXE%
echo 数据库: %DB_NAME%
echo SQL文件: %SQL_FILE%
echo.

if not exist "%MYSQL_EXE%" (
    echo 错误: 找不到MySQL客户端
    exit /b 1
)

if not exist "%SQL_FILE%" (
    echo 错误: 找不到SQL文件
    exit /b 1
)

echo 正在执行SQL文件...
echo.

type "%SQL_FILE%" | "%MYSQL_EXE%" -u root --default-character-set=utf8mb4 %DB_NAME%

if %errorlevel% equ 0 (
    echo.
    echo ============================================================
    echo   执行完成！
    echo ============================================================
    echo.
    echo 正在验证表注释...
    echo.
    
    "%MYSQL_EXE%" -u root --default-character-set=utf8mb4 %DB_NAME% -e "SELECT TABLE_NAME AS '表名', TABLE_COMMENT AS '表注释' FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME LIKE 'scm_%%' ORDER BY TABLE_NAME;"
    
    echo.
    echo ============================================================
    echo   如果上面显示的表注释是中文，说明修复成功！
    echo ============================================================
) else (
    echo.
    echo 错误: 执行失败
    exit /b 1
)

pause
