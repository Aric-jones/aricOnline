#!/bin/bash
# ============================================================
#  博客一键部署脚本
#  用法: sh blog-start.sh [up|down|restart|logs]
# ============================================================

set -e
cd "$(dirname "$0")"

# 检查 .env 文件
if [ ! -f .env ]; then
    echo "❌ 未找到 .env 文件，请先执行: cp .env.example .env"
    echo "   然后编辑 .env 填入真实的密码和密钥"
    exit 1
fi

ACTION=${1:-up}

case $ACTION in
    up)
        echo "🚀 启动博客服务..."
        docker-compose up -d --build
        echo ""
        echo "✅ 部署完成！"
        echo "   博客前台：http://localhost"
        echo "   后台管理：http://localhost/admin"
        echo "   后端 API：http://localhost:8080"
        echo ""
        echo "📋 查看日志: sh blog-start.sh logs"
        ;;
    down)
        echo "⏹️  停止博客服务..."
        docker-compose down
        echo "✅ 已停止"
        ;;
    restart)
        echo "🔄 重启博客服务..."
        docker-compose down
        docker-compose up -d --build
        echo "✅ 重启完成"
        ;;
    logs)
        docker-compose logs -f --tail=100
        ;;
    status)
        docker-compose ps
        ;;
    *)
        echo "用法: sh blog-start.sh [up|down|restart|logs|status]"
        echo "  up      - 构建并启动所有服务（默认）"
        echo "  down    - 停止并移除所有容器"
        echo "  restart - 重启所有服务"
        echo "  logs    - 查看实时日志"

        echo "  status  - 查看容器状态"
        ;;
esac
