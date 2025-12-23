#!/bin/bash

# 门诊挂号管理系统 - 启动脚本

cd "$(dirname "$0")/.." || exit 1

echo "======================================"
echo "  启动门诊挂号管理系统..."
echo "======================================"

# 确保logs目录存在
mkdir -p logs

# 启动应用
./mvnw spring-boot:run
