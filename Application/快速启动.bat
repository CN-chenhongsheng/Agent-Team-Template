@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo   Backend - Quick Start
echo ========================================
echo.
echo Project directory: %cd%
echo.

if not exist "%~dp0pom.xml" (
    echo [ERROR] pom.xml was not found in: %~dp0
    pause
    exit /b 1
)

echo [1/4] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found. Please install JDK 21 first.
    pause
    exit /b 1
)
echo [OK] Java is available.

echo.
echo [2/4] Checking MySQL and Redis...
echo Please make sure these services are running:
echo   - MySQL: localhost:3306
echo   - Redis: localhost:6379
echo.

set REDIS_CLI=redis-cli
set REDIS_SERVER=redis-server

where redis-cli >nul 2>&1
if errorlevel 1 (
    if exist "E:\Redis\redis-cli.exe" (
        set REDIS_CLI=E:\Redis\redis-cli.exe
    )
)

where redis-server >nul 2>&1
if errorlevel 1 (
    if exist "E:\Redis\redis-server.exe" (
        set REDIS_SERVER=E:\Redis\redis-server.exe
    )
)

"!REDIS_CLI!" ping >nul 2>&1
if errorlevel 1 (
    echo [WARN] Redis did not respond to PING. Trying to start Redis...
    start "Redis Server" /min "!REDIS_SERVER!"
    timeout /t 2 /nobreak >nul
    "!REDIS_CLI!" ping >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Redis still did not respond. Please start Redis manually.
        echo         Command: "!REDIS_SERVER!"
        pause
        exit /b 1
    ) else (
        echo [OK] Redis started and responded to PING.
    )
) else (
    echo [OK] Redis responded to PING.
)

sc query MySQL | findstr /I "RUNNING" >nul 2>&1
if errorlevel 1 (
    echo [WARN] MySQL service is not running or the service name is not MySQL.
) else (
    echo [OK] MySQL service is running.
)

echo.
echo [3/4] Releasing port 8080...
set PORT_BUSY=0
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":8080 .*LISTENING"') do (
    set PORT_BUSY=1
    echo Found process %%P using port 8080. Killing it...
    taskkill /F /PID %%P >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Failed to kill process %%P. You may need to run this script as administrator.
    ) else (
        echo [OK] Process %%P was killed.
    )
)

if "!PORT_BUSY!"=="0" (
    echo [OK] Port 8080 is free.
) else (
    timeout /t 2 /nobreak >nul
)

echo.
echo [4/4] Starting Spring Boot application...
echo Downloading dependencies and starting the backend...
echo.

call mvn -f "%~dp0pom.xml" spring-boot:run

pause
