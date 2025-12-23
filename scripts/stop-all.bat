@echo off

echo ==========================================
echo   Hospital Registration System - Stop All
echo ==========================================
echo.

echo [INFO] Stopping backend Java process...
taskkill /F /IM java.exe 2>nul
if %errorlevel%==0 (
    echo [SUCCESS] Backend stopped
) else (
    echo [INFO] Backend not running
)

echo.
echo [INFO] Stopping frontend Node process...
taskkill /F /IM node.exe 2>nul
if %errorlevel%==0 (
    echo [SUCCESS] Frontend stopped
) else (
    echo [INFO] Frontend not running
)

echo.
echo [DONE] All services stopped
echo.
pause
