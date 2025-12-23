#!/bin/bash
# 前端构建脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

cd "$FRONTEND_DIR"

echo "=========================================="
echo "  门诊挂号系统 - 前端构建"
echo "=========================================="
echo ""

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo "[INFO] 正在安装依赖..."
    npm install
fi

echo "[INFO] 开始构建..."
npm run build

if [ $? -eq 0 ]; then
    echo ""
    echo "[SUCCESS] 构建完成！输出目录: $FRONTEND_DIR/dist"
else
    echo ""
    echo "[ERROR] 构建失败"
    exit 1
fi
