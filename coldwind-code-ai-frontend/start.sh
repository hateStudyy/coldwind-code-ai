#!/bin/bash

echo "🚀 启动 Coldwind Code AI Frontend 项目..."

# 检查 Node.js 版本
echo "📋 检查 Node.js 版本..."
node_version=$(node --version)
echo "当前 Node.js 版本: $node_version"

# 检查是否安装了依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装项目依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败，请检查网络连接和 Node.js 版本"
        exit 1
    fi
    echo "✅ 依赖安装完成"
else
    echo "✅ 依赖已安装"
fi

# 启动开发服务器
echo "🌐 启动开发服务器..."
echo "项目将在 http://localhost:5173 启动"
echo "按 Ctrl+C 停止服务器"
echo ""

npm run dev
