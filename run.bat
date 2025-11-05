@echo off
echo Compiling Java files...
javac -cp "lib/*" src/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running the application...
java -cp "src;lib/*" Main
pause