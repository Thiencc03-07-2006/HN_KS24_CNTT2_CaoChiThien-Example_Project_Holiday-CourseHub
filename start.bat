@echo off
set "JAVA_HOME="
title CourseHub Dev Environment Orchestrator
echo ============================================================
echo      CourseHub - Automated Development Environment Launcher
echo ============================================================
echo.

:: 1. Check if Docker Daemon is running
echo [1/6] Checking Docker status...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop first and try again.
    echo.
    pause
    exit /b 1
)
echo [INFO] Docker is running.

:: 2. Boot Docker Compose services
echo.
echo [2/6] Starting database and cache services (MySQL, Redis)...
docker compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start Docker Compose services!
    pause
    exit /b 1
)

:: 3. Wait for MySQL to become healthy
echo.
echo [3/6] Waiting for MySQL container (coursehub-mysql) to become healthy...
:wait_mysql
for /f "tokens=*" %%i in ('docker inspect --format^="{{.State.Health.Status}}" coursehub-mysql 2^>nul') do set mysql_status=%%i
if not "%mysql_status%"=="healthy" (
    timeout /t 2 /nobreak >nul
    goto wait_mysql
)
echo [INFO] MySQL is healthy!

:: 4. Wait for Redis to become healthy
echo.
echo [4/6] Waiting for Redis container (coursehub-redis) to become healthy...
:wait_redis
for /f "tokens=*" %%i in ('docker inspect --format^="{{.State.Health.Status}}" coursehub-redis 2^>nul') do set redis_status=%%i
if not "%redis_status%"=="healthy" (
    timeout /t 2 /nobreak >nul
    goto wait_redis
)
echo [INFO] Redis is healthy!

:: 5. Install Frontend dependencies if node_modules is missing
echo.
echo [5/6] Checking frontend dependencies...
if not exist "coursehub-frontend\node_modules\" (
    echo [INFO] node_modules not found in coursehub-frontend. Installing dependencies...
    cd coursehub-frontend && call npm install && cd ..
    echo [INFO] Dependencies installed successfully.
) else (
    echo [INFO] frontend dependencies are already installed.
)

:: 6. Launch Backend, Frontend and Browser
echo.
echo [6/6] Launching backend and frontend applications...
echo [INFO] Launching backend (Spring Boot) in a new console...
start "Backend (Spring Boot)" cmd /k "cd coursehub && gradlew.bat bootRun"

echo [INFO] Launching frontend (Vite) in a new console...
start "Frontend (Vite)" cmd /k "cd coursehub-frontend && npm run dev"

echo.
echo ============================================================
echo  All services are launching. Opening your browser shortly...
echo ============================================================
timeout /t 5 /nobreak >nul
start http://localhost:5173
echo [INFO] Launcher script completed. You can close this window.
timeout /t 3 /nobreak >nul
exit
