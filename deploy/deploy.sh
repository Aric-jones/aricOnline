#!/bin/bash
# ============================================================
#  博客一键部署脚本（OpenCloudOS）
#  使用方法：
#    1. 确保项目已上传到服务器（例如 /opt/aricOnline）
#    2. cd <项目路径>/deploy
#    3. chmod +x deploy.sh
#    4. ./deploy.sh
# ============================================================

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
info() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo "=========================================="
echo "  博客一键部署脚本（OpenCloudOS）"
echo "=========================================="
echo ""

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then 
    error "请使用 root 用户运行此脚本"
    exit 1
fi

# ========== 第一步：安装 Docker ==========
info "检查 Docker 安装状态..."
if ! command -v docker &> /dev/null; then
    warn "Docker 未安装，开始安装..."
    
    # 安装必要的工具
    info "安装必要工具..."
    dnf install -y dnf-plugins-core device-mapper-persistent-data lvm2
    
    # 添加 Docker 仓库
    info "添加 Docker 仓库..."
    if ! dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo 2>/dev/null; then
        warn "阿里云镜像失败，尝试官方源..."
        dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    fi
    
    # 更新缓存
    info "更新软件包缓存..."
    dnf makecache
    
    # 安装 Docker + Compose 插件（全部通过 dnf，无需 curl 下载）
    info "安装 Docker 和 Docker Compose 插件..."
    dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    
    # 启动 Docker
    info "启动 Docker 服务..."
    systemctl start docker
    systemctl enable docker
    
    # 配置 Docker 镜像加速
    info "配置 Docker 镜像加速..."
    mkdir -p /etc/docker
    
    # 检查是否已有配置
    if [ -f "/etc/docker/daemon.json" ]; then
        warn "Docker 配置文件已存在，备份为 daemon.json.bak"
        cp /etc/docker/daemon.json /etc/docker/daemon.json.bak
    fi
    
    cat > /etc/docker/daemon.json << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com",
    "https://dockerhub.azk8s.cn",
    "https://reg-mirror.qiniu.com"
  ],
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5
}
DOCKER_EOF
    systemctl daemon-reload
    systemctl restart docker
    
    # 等待 Docker 重启完成
    sleep 3
    
    info "Docker 镜像加速器已配置，包含多个国内镜像源"
    
    info "Docker 安装完成: $(docker --version)"
else
    info "Docker 已安装: $(docker --version)"
fi

# ========== 第二步：检查 Docker Compose ==========
# 使用 docker compose 插件（通过 dnf 安装，无需 curl 下载）
info "检查 Docker Compose 插件..."
if ! docker compose version &> /dev/null; then
    warn "Docker Compose 插件未安装，通过 dnf 安装..."
    # 确保有 Docker 仓库
    if ! dnf repolist 2>/dev/null | grep -q docker; then
        info "添加 Docker 仓库..."
        dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo 2>/dev/null || \
        dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        dnf makecache
    fi
    dnf install -y docker-compose-plugin || {
        error "Docker Compose 安装失败，请检查网络和仓库配置"
        exit 1
    }
    info "Docker Compose 安装完成: $(docker compose version)"
else
    info "Docker Compose 已安装: $(docker compose version)"
fi

# 统一使用 docker compose（带空格）命令
COMPOSE="docker compose"

# ========== 第三步：检查项目文件 ==========
info "检查项目文件..."
if [ ! -f "docker-compose.yml" ]; then
    error "docker-compose.yml 文件不存在，请确保在 deploy 目录下运行此脚本"
    exit 1
fi

if [ ! -f "Dockerfile" ] || [ ! -f "Dockerfile.nginx" ]; then
    error "Dockerfile 文件不存在，请确保项目文件完整"
    exit 1
fi

# ========== 第四步：配置环境变量 ==========
info "检查环境变量配置..."
if [ ! -f ".env" ]; then
    warn ".env 文件不存在，从模板创建..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        info "已创建 .env 文件"
        warn "请先编辑 .env 文件，配置 MySQL、Redis 密码等信息"
        echo ""
        echo "编辑命令: vim .env"
        echo ""
        echo "必须配置的项："
        echo "  - MYSQL_PASSWORD: MySQL 密码"
        echo "  - REDIS_PASSWORD: Redis 密码"
        echo "  - BLOG_URL: 博客地址（已自动设置为 http://212.64.28.65/）"
        echo "  - UPLOAD_STRATEGY: 文件上传方式（oss/cos/local）"
        echo ""
        exit 1
    else
        error ".env.example 文件不存在"
        exit 1
    fi
fi

# 加载环境变量
source .env

# 检查必要的配置
if [ -z "$MYSQL_PASSWORD" ] || [ "$MYSQL_PASSWORD" = "your_password" ]; then
    error "请先配置 .env 文件中的 MYSQL_PASSWORD"
    exit 1
fi

if [ -z "$REDIS_PASSWORD" ]; then
    warn "REDIS_PASSWORD 未设置，将使用空密码（不推荐）"
fi

# ========== 第五步：停止旧容器 ==========
info "停止旧容器（如果存在）..."
$COMPOSE down 2>/dev/null || true

# ========== 第六步：预拉取基础镜像 ==========
info "预拉取 Docker 镜像（使用镜像加速器）..."

# 拉取 MySQL 镜像（带重试）
info "拉取 MySQL 镜像..."
MYSQL_PULLED=false
for i in {1..3}; do
    if docker pull mysql:8.0 2>/dev/null; then
        info "MySQL 镜像拉取成功"
        MYSQL_PULLED=true
        break
    else
        warn "MySQL 镜像拉取失败，重试 $i/3..."
        sleep 5
    fi
done

if [ "$MYSQL_PULLED" = false ]; then
    error "MySQL 镜像拉取失败，请检查网络连接或 Docker 镜像加速器配置"
    error "可以手动执行: docker pull mysql:8.0"
    exit 1
fi

# 拉取 Redis 镜像（带重试）
info "拉取 Redis 镜像..."
REDIS_PULLED=false
for i in {1..3}; do
    if docker pull redis:7-alpine 2>/dev/null; then
        info "Redis 镜像拉取成功"
        REDIS_PULLED=true
        break
    else
        warn "Redis 镜像拉取失败，重试 $i/3..."
        sleep 5
    fi
done

if [ "$REDIS_PULLED" = false ]; then
    error "Redis 镜像拉取失败，请检查网络连接或 Docker 镜像加速器配置"
    error "可以手动执行: docker pull redis:7-alpine"
    exit 1
fi

# 拉取 Nginx 镜像（用于前端构建）
info "拉取 Nginx 镜像..."
docker pull nginx:1.25-alpine || warn "Nginx 镜像拉取失败，将在构建时重试"

# 拉取构建镜像
info "拉取构建所需的基础镜像..."
docker pull node:18-alpine || warn "Node 镜像拉取失败，将在构建时重试"
docker pull maven:3.8-openjdk-11 || warn "Maven 镜像拉取失败，将在构建时重试"
docker pull eclipse-temurin:11-jre || warn "Temurin JRE 镜像拉取失败，将在构建时重试"

# ========== 第七步：构建并启动服务 ==========
info "开始构建并启动服务（这可能需要 5-10 分钟）..."
info "正在构建 Docker 镜像，请耐心等待..."

$COMPOSE up -d --build

# 等待服务启动
info "等待服务启动..."
sleep 15

# ========== 第八步：检查服务状态 ==========
echo ""
echo "=========================================="
info "服务状态检查"
echo "=========================================="
$COMPOSE ps

# 等待 MySQL 完全启动
echo ""
info "等待 MySQL 完全启动（最多 60 秒）..."
MYSQL_READY=false
for i in {1..60}; do
    if docker exec blog-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
        info "MySQL 已就绪"
        MYSQL_READY=true
        break
    fi
    echo -n "."
    sleep 1
done
echo ""

if [ "$MYSQL_READY" = false ]; then
    warn "MySQL 启动超时，请检查日志: $COMPOSE logs mysql"
fi

# ========== 第九步：健康检查 ==========
echo ""
echo "=========================================="
info "健康检查"
echo "=========================================="

# 检查 MySQL
if docker exec blog-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
    info "MySQL: ✅ 正常"
else
    error "MySQL: ❌ 异常"
    $COMPOSE logs mysql | tail -20
fi

# 检查 Redis
if docker exec blog-redis redis-cli -a "${REDIS_PASSWORD:-}" ping 2>/dev/null | grep -q PONG; then
    info "Redis: ✅ 正常"
else
    error "Redis: ❌ 异常"
    $COMPOSE logs redis | tail -20
fi

# 检查后端
SERVER_PORT=${SERVER_PORT:-8000}
sleep 5  # 给后端更多启动时间
if curl -s http://localhost:${SERVER_PORT}/ > /dev/null 2>&1; then
    info "后端 API: ✅ 正常"
else
    warn "后端 API: ⚠️  可能还在启动中（稍后重试）"
    $COMPOSE logs blog-server | tail -20
fi

# 检查 Nginx
if curl -s http://localhost/ > /dev/null 2>&1; then
    info "Nginx: ✅ 正常"
else
    warn "Nginx: ⚠️  可能还在启动中（稍后重试）"
    $COMPOSE logs nginx | tail -20
fi

# ========== 第十步：显示访问地址 ==========
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "212.64.28.65")

echo ""
echo "=========================================="
info "🎉 部署完成！"
echo "=========================================="
echo ""
echo "📝 访问地址："
echo "   博客前台: http://${SERVER_IP}/"
echo "   后台管理: http://${SERVER_IP}/admin"
echo "   后端 API: http://${SERVER_IP}:${SERVER_PORT}/"
echo ""
echo "📋 常用命令："
echo "   查看日志: docker compose logs -f"
echo "   查看单个服务日志: docker compose logs -f blog-server"
echo "   重启服务: docker compose restart"
echo "   停止服务: docker compose down"
echo "   查看状态: docker compose ps"
echo "   进入 MySQL: docker exec -it blog-mysql mysql -uroot -p"
echo ""
echo "📖 如果遇到问题，查看日志: docker compose logs -f"
echo ""
