@echo off
title Hospital Registration - Backend

echo ==========================================
echo   Hospital Registration System - Backend
echo ==========================================
echo.

cd /d %~dp0..

echo [INFO] Starting Spring Boot backend...
echo [INFO] Port: 8080
echo.

call mvnw.cmd spring-boot:run

pause
