@echo off
echo Starting web server...
javac -cp "lib/*" src/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running web server on port 8080...
java -cp "src;lib/*" WebServer
pause