@echo off
chcp 65001 >nul
echo ========================================
echo   系统后端 - 快速启动脚本
echo ========================================
echo.

echo [1/3] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到Java，请先安装JDK 21
    pause
    exit /b 1
)
echo ✅ Java环境正常

echo.
echo [2/3] 检查MySQL和Redis...
echo ⚠️  请确保MySQL和Redis已启动
echo    - MySQL: localhost:3306
echo    - Redis: localhost:6379
echo.
pause

echo.
echo [3/3] 启动Spring Boot应用...
echo 📦 正在下载依赖并启动...
echo.

call mvn spring-boot:run

pause

