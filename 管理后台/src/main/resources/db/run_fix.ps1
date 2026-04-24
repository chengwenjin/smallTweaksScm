$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$mysql = "D:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe"
$sqlFile = "f:\AiCoding\traeWork\smallTweaksScm\管理后台\src\main\resources\db\final_fix.sql"
$db = "small_tweaks_scm"

Write-Host "Reading SQL file: $sqlFile"

$sqlContent = Get-Content -Path $sqlFile -Raw -Encoding UTF8

Write-Host "Executing SQL..."

$sqlContent | & $mysql -u root --default-character-set=utf8mb4 $db

Write-Host "Done!"
