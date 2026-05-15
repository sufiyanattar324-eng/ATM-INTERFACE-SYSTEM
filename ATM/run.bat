@echo off
echo ==========================================
echo   ATM Interface System - Build Script
echo ==========================================
echo.

REM --- AUTO-START MYSQL DATABASE ---
echo Checking MySQL Database Server...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL is already running in the background.
) else (
    echo [STARTING] MySQL is not running. Attempting to start it now...
    if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" (
        start /B "" "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.4\my.ini" --console >nul 2>&1
        timeout /t 4 /nobreak >nul
        echo [OK] MySQL started successfully!
    ) else (
        echo [WARNING] MySQL 8.4 not found at standard path!
        echo The database might fail to connect unless already running.
    )
)
echo.

REM Check if lib folder has MySQL connector
if not exist "lib\mysql-connector-j*.jar" (
    echo [ERROR] MySQL Connector JAR not found in lib\ folder!
    echo.
    echo Please download it from:
    echo https://dev.mysql.com/downloads/connector/j/
    echo.
    echo Place the .jar file in the "lib" folder.
    pause
    exit /b 1
)

REM Get the connector JAR name
for %%f in (lib\mysql-connector-j*.jar) do set MYSQL_JAR=%%f
echo Found MySQL Connector: %MYSQL_JAR%

REM Create output directory
if not exist "out" mkdir out

echo.
echo [1/2] Compiling Java files...
javac -d out -cp "%MYSQL_JAR%" src\atm\model\*.java src\atm\db\*.java src\atm\dao\*.java src\atm\gui\*.java src\atm\Main.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed! Check errors above.
    pause
    exit /b 1
)

echo [2/2] Compilation successful!
echo.
echo ==========================================
echo   Running ATM Interface System...
echo ==========================================
echo.
java -cp "out;%MYSQL_JAR%" atm.Main

pause
