@echo off
title Hospital Registration - Build Backend

echo ==========================================
echo   Hospital Registration System - Build
echo ==========================================
echo.

cd /d %~dp0..

echo [INFO] Compiling backend...
call mvnw.cmd clean compile -DskipTests

if %errorlevel%==0 (
    echo.
    echo [SUCCESS] Build succeeded!
) else (
    echo.
    echo [ERROR] Build failed!
)

echo.
pause
