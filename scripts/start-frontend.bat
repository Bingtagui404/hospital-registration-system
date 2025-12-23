@echo off
title Hospital Registration - Frontend

echo ==========================================
echo   Hospital Registration System - Frontend
echo ==========================================
echo.

cd /d %~dp0..\frontend

if not exist node_modules (
    echo [INFO] Installing dependencies...
    call npm install
)

echo [INFO] Starting Vite dev server...
echo [INFO] URL: http://localhost:5173
echo.

call npx vite

pause
