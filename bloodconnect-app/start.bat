@echo off
REM BloodConnect Quick Start Script for Windows

echo.
echo ==========================================
echo   BloodConnect - Quick Start
echo ==========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed. Please install Docker Desktop.
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Compose is not installed. Please install Docker Desktop.
    pause
    exit /b 1
)

echo SUCCESS: Docker and Docker Compose detected
echo.

REM Navigate to docker directory
cd .docker

echo Starting BloodConnect...
echo.
echo Building and starting services...
docker-compose up --build

echo.
echo ==========================================
echo   BloodConnect is running!
echo ==========================================
echo.
echo Frontend:  http://localhost:3000
echo Backend:   http://localhost:8080
echo Database:  localhost:5432
echo.
echo Press Ctrl+C to stop
pause
