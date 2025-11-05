@echo off
REM start-all.bat - One-file launcher for Question Paper Management System
REM Usage: double-click this file or run from CMD: start-all.bat

setlocal enabledelayedexpansion

echo ==================================================
echo Question Paper Management System - One-click launcher
echo ==================================================

:: 1) Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found in PATH. Install JDK and try again.
    pause
    exit /b 1
)
echo Java found.

:: 2) Ensure lib folder and download dependencies if missing
if not exist "%~dp0lib" mkdir "%~dp0lib"
echo Checking libraries in "%~dp0lib"...

:: jakarta.mail
if not exist "%~dp0lib\jakarta.mail-2.0.1.jar" (
    echo Downloading jakarta.mail-2.0.1.jar...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar' -OutFile '%~dp0lib\jakarta.mail-2.0.1.jar' -UseBasicParsing" 2>nul || (
        echo Failed to download jakarta.mail-2.0.1.jar. Please download manually into the lib folder.
    )
) else echo Found jakarta.mail-2.0.1.jar

:: jakarta.activation
if not exist "%~dp0lib\jakarta.activation-2.0.1.jar" (
    echo Downloading jakarta.activation-2.0.1.jar...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar' -OutFile '%~dp0lib\jakarta.activation-2.0.1.jar' -UseBasicParsing" 2>nul || (
        echo Failed to download jakarta.activation-2.0.1.jar. Please download manually into the lib folder.
    )
) else echo Found jakarta.activation-2.0.1.jar

:: MySQL Connector
if not exist "%~dp0lib\mysql-connector-j-8.0.33.jar" (
    echo Downloading mysql-connector-j-8.0.33.jar...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile '%~dp0lib\mysql-connector-j-8.0.33.jar' -UseBasicParsing" 2>nul || (
        echo Failed to download mysql-connector-j-8.0.33.jar. Please download manually into the lib folder.
    )
) else echo Found mysql-connector-j-8.0.33.jar

:: 3) Compile Java sources
echo Compiling Java sources...
pushd "%~dp0src"
javac -cp ".;..\lib\*" *.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed. See output above.
    pause
    popd
    exit /b 1
)
popd
echo Compilation successful.

:: 4) Database setup (manual step)
echo Checking for database setup script...
if exist "%~dp0setup-database.sql" (
    echo Found setup-database.sql.
    echo NOTE: This launcher does not automatically run the SQL to avoid environment-specific issues.
    echo Please run the following command manually if your MySQL client is available:
    echo mysql -u root -p < "%~dp0setup-database.sql"
) else (
    echo setup-database.sql not found; skipping DB creation.
)

:: 5) Start WebServer in a new window so logs are visible
echo Starting WebServer (new window)...
start "QPMS WebServer" cmd /k "cd /d "%~dp0src" && java -cp .;..\lib\* WebServer"

:: Give server a moment to start
echo Waiting for server to start...
timeout /t 2 /nobreak >nul

:: 6) Open frontend in default browser
echo Opening web interface...
start "" "http://localhost:8080/frontend/index.html"

echo All done. Check the "QPMS WebServer" window for logs (requests, email errors, file lookups).
echo To stop the server: close the "QPMS WebServer" window or press Ctrl+C there.

endlocal
exit /b 0
