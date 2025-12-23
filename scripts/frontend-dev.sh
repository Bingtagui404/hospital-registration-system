#!/bin/bash
# 前端开发服务器启动脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

cd "$FRONTEND_DIR"

echo "=========================================="
echo "  门诊挂号系统 - 前端开发服务器"
echo "=========================================="
echo ""

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo "[INFO] 正在安装依赖..."
    npm install
fi

echo "[INFO] 启动开发服务器..."
echo "[INFO] 访问地址: http://localhost:5173"
echo ""

npm run dev
