@echo off
echo ==========================================
echo   ATM System - Compile Only
echo ==========================================
echo.

REM Find MySQL connector
for %%f in (lib\mysql-connector-j*.jar) do set MYSQL_JAR=%%f

if not defined MYSQL_JAR (
    echo [ERROR] MySQL Connector JAR not found in lib\ folder!
    pause
    exit /b 1
)

if not exist "out" mkdir out

echo Compiling...
javac -d out -cp "%MYSQL_JAR%" src\atm\model\*.java src\atm\db\*.java src\atm\dao\*.java src\atm\gui\*.java src\atm\Main.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
) else (
    echo [SUCCESS] All files compiled to out\ folder.
)

pause
