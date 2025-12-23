@echo off

echo ==========================================
echo   Hospital Registration System - Start All
echo ==========================================
echo.

cd /d %~dp0

echo [INFO] Starting backend service...
start "Backend-Spring Boot" cmd /k start-backend.bat

timeout /t 3 /nobreak >nul

echo [INFO] Starting frontend service...
start "Frontend-Vite" cmd /k start-frontend.bat

echo.
echo [SUCCESS] Services starting...
echo.
echo   Backend URL:  http://localhost:8080
echo   Frontend URL: http://localhost:5173
echo.
echo Press any key to close this window...
pause >nul
