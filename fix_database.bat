@echo off
echo ============================================
echo   Student Registration DB Fix Script
echo ============================================
echo.
echo This will DROP and RECREATE the registration_db database.
echo ALL existing data will be deleted.
echo.
set /p MYSQL_PASS=Enter your MySQL root password: 
echo.
echo Step 1: Dropping old database...
mysql -u root -p%MYSQL_PASS% -e "DROP DATABASE IF EXISTS registration_db;"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Could not connect to MySQL. Check your password and try again.
    pause
    exit /b 1
)
echo Done.

echo Step 2: Recreating database from database_setup.sql...
mysql -u root -p%MYSQL_PASS% < database_setup.sql
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to run database_setup.sql.
    pause
    exit /b 1
)
echo Done.

echo.
echo ============================================
echo   SUCCESS! Database recreated cleanly.
echo   You can now run the application.
echo ============================================
pause
