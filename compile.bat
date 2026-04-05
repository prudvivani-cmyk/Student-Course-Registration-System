@echo off
echo Compiling the Student Course Registration System...
mkdir bin 2>nul
javac -cp "lib\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar;lib\rs2xml.jar" -d bin src\models\*.java src\util\*.java src\dao\*.java src\ui\*.java src\main\*.java
if %ERRORLEVEL% == 0 (
    echo.
    echo *** Compilation successful! ***
    echo Run:  run.bat
) else (
    echo.
    echo *** Compilation FAILED. See errors above. ***
)
pause
