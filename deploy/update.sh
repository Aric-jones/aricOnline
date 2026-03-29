#!/bin/bash
# ============================================================
#  博客热更新脚本 — 独立更新前端/后端，不影响数据库
#  使用方法：
#    cd /opt/aricOnline/deploy
#    chmod +x update.sh
#    ./update.sh          # 交互式选择
#    ./update.sh backend  # 直接更新后端
#    ./update.sh frontend # 直接更新前端
#    ./update.sh all      # 前后端一起更新
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
title() { echo -e "${CYAN}$1${NC}"; }

COMPOSE="docker compose"

if [ ! -f "docker-compose.yml" ]; then
    error "请在 deploy/ 目录下运行此脚本"
    exit 1
fi

show_status() {
    echo ""
    title "📊 当前服务状态："
    $COMPOSE ps
    echo ""
}

update_backend() {
    info "🔧 开始更新后端 (blog-server)..."
    info "MySQL 和 Redis 不受影响，保持运行。"
    echo ""

    local start=$(date +%s)
    $COMPOSE up -d --build --no-deps blog-server
    local end=$(date +%s)
    local elapsed=$((end - start))

    echo ""
    info "✅ 后端更新完成！耗时 ${elapsed} 秒"
    info "等待 Spring Boot 启动..."
    sleep 10

    if docker exec blog-server curl -sf http://localhost:8000/ > /dev/null 2>&1; then
        info "后端 API: ✅ 正常"
    else
        warn "后端 API: ⚠️ 可能还在启动中（约 30 秒）"
        warn "查看日志: docker compose logs -f blog-server"
    fi
}

update_frontend() {
    info "🎨 开始更新前端 (nginx)..."
    info "后端 API、MySQL、Redis 不受影响，保持运行。"
    echo ""

    local start=$(date +%s)
    $COMPOSE up -d --build --no-deps nginx
    local end=$(date +%s)
    local elapsed=$((end - start))

    echo ""
    info "✅ 前端更新完成！耗时 ${elapsed} 秒"

    if curl -sf http://localhost/ > /dev/null 2>&1; then
        info "前端页面: ✅ 正常"
    else
        warn "前端页面: ⚠️ 请检查: docker compose logs -f nginx"
    fi
}

update_all() {
    info "🚀 开始更新前端 + 后端..."
    info "MySQL 和 Redis 不受影响，保持运行。"
    echo ""

    local start=$(date +%s)
    $COMPOSE up -d --build --no-deps blog-server nginx
    local end=$(date +%s)
    local elapsed=$((end - start))

    echo ""
    info "✅ 前后端更新完成！耗时 ${elapsed} 秒"
    info "等待服务启动..."
    sleep 10
    show_status
}

pull_code() {
    info "📥 拉取最新代码..."
    cd ..
    if git pull; then
        info "代码更新成功"
    else
        error "代码拉取失败，请手动处理"
        exit 1
    fi
    cd deploy
}

# 清理旧镜像（释放磁盘空间）
clean_images() {
    info "🧹 清理无用的旧镜像..."
    docker image prune -f
    info "清理完成"
}

ACTION=${1:-""}

if [ -n "$ACTION" ]; then
    case "$ACTION" in
        backend|server|api)   show_status; update_backend ;;
        frontend|nginx|web)   show_status; update_frontend ;;
        all|both)             show_status; update_all ;;
        pull)                 pull_code ;;
        status)               show_status ;;
        clean)                clean_images ;;
        *)
            error "未知参数: $ACTION"
            echo "用法: $0 [backend|frontend|all|pull|status|clean]"
            exit 1
            ;;
    esac
else
    echo ""
    title "=========================================="
    title "  博客热更新工具"
    title "=========================================="
    show_status

    echo "请选择操作："
    echo ""
    echo "  1) 🔧 仅更新后端 (Java API)"
    echo "  2) 🎨 仅更新前端 (Vue 博客 + 后台管理)"
    echo "  3) 🚀 前后端一起更新"
    echo "  4) 📥 拉取代码 + 前后端更新"
    echo "  5) 📊 查看服务状态"
    echo "  6) 🧹 清理旧镜像"
    echo "  0) 退出"
    echo ""
    read -p "请输入选项 [0-6]: " choice

    case $choice in
        1) update_backend ;;
        2) update_frontend ;;
        3) update_all ;;
        4) pull_code && update_all ;;
        5) show_status ;;
        6) clean_images ;;
        0) echo "退出"; exit 0 ;;
        *) error "无效选项"; exit 1 ;;
    esac
fi

echo ""
info "🎉 操作完成！"
echo ""
