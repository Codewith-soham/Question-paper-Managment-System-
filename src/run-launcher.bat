@echo off
echo Compiling LaunchQPMS...
javac -cp ".;../lib/*" LaunchQPMS.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Running LaunchQPMS...
java -cp ".;../lib/*" LaunchQPMS
pause