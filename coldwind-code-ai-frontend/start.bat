@echo off
chcp 65001 >nul
echo 🚀 启动 Coldwind Code AI Frontend 项目...

echo 📋 检查 Node.js 版本...
node --version
if %errorlevel% neq 0 (
    echo ❌ 未找到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

echo.
echo 📦 检查项目依赖...
if not exist "node_modules" (
    echo 安装项目依赖...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 依赖安装失败，请检查网络连接和 Node.js 版本
        pause
        exit /b 1
    )
    echo ✅ 依赖安装完成
) else (
    echo ✅ 依赖已安装
)

echo.
echo 🌐 启动开发服务器...
echo 项目将在 http://localhost:5173 启动
echo 按 Ctrl+C 停止服务器
echo.

npm run dev
pause
