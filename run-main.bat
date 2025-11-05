@echo off
REM Run the console Main application for Question Paper Management System
REM This script compiles Main.java (if needed) and runs it in the console.

pushd "%~dp0src"
echo[Compiling] javac -cp .;..\lib\* Main.java
javac -cp .;..\lib\* Main.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    popd
    pause
    exit /b 1
)

echo[Running] java -cp .;..\lib\* Main
java -cp .;..\lib\* Main

popd
necho.
echo Press any key to close...
pause >nul
