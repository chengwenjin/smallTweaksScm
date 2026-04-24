@echo off
chcp 65001 >nul

set MYSQL_JAR=c:\Users\Laptop\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar
set CLASSES_DIR=f:\AiCoding\traeWork\smallTweaksScm\管理后台\target\classes

echo ============================================================
echo   修复SCM数据库中文注释乱码
echo ============================================================
echo.
echo MySQL驱动: %MYSQL_JAR%
echo 编译目录: %CLASSES_DIR%
echo.

if not exist "%MYSQL_JAR%" (
    echo 错误: 找不到MySQL驱动
    pause
    exit /b 1
)

if not exist "%CLASSES_DIR%" (
    echo 错误: 找不到编译目录
    pause
    exit /b 1
)

echo 正在执行修复程序...
echo.

java -cp "%CLASSES_DIR%;%MYSQL_JAR%" com.baserbac.scm.util.FixScmComments

echo.
echo ============================================================
echo   执行完成！
echo ============================================================
pause
