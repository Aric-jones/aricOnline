# 博客一键部署指南

## 🚀 快速开始

### 第一步：上传项目到服务器

```bash
# 方法一：使用 Git
cd /opt
git clone 你的仓库地址 blog

# 方法二：使用 SCP（本地 Windows PowerShell）
scp -r e:\blog root@212.64.28.65:/opt/blog
```

### 第二步：配置环境变量

```bash
cd /opt/blog/deploy
cp .env.example .env
vim .env
```

**必须修改的配置**：
- `MYSQL_PASSWORD`: 设置一个强密码
- `REDIS_PASSWORD`: 设置一个强密码
- `BLOG_URL`: 已自动设置为 `http://212.64.28.65/`
- `UPLOAD_STRATEGY`: 配置文件上传（oss/cos/local）

### 第三步：一键部署

```bash
cd /opt/blog/deploy
chmod +x deploy.sh
./deploy.sh
```

脚本会自动：
1. ✅ 检查并安装 Docker（如果没有）
2. ✅ 检查并安装 Docker Compose（如果没有）
3. ✅ 构建并启动所有服务（MySQL + Redis + 后端 + Nginx）
4. ✅ 检查服务健康状态

**等待 5-10 分钟**，部署完成后会显示访问地址。

## 📝 访问地址

- **博客前台**: http://212.64.28.65/
- **后台管理**: http://212.64.28.65/admin
- **后端 API**: http://212.64.28.65:8000/

## 🔧 常用命令

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看单个服务日志
docker-compose logs -f blog-server
docker-compose logs -f blog-nginx
docker-compose logs -f mysql

# 重启服务
docker-compose restart

# 重启单个服务
docker-compose restart blog-server

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps

# 进入 MySQL
docker exec -it blog-mysql mysql -uroot -p你的密码

# 进入 Redis
docker exec -it blog-redis redis-cli -a 你的密码
```

## 🐛 常见问题

### 问题1：部署脚本执行失败

**查看详细错误**：
```bash
# 查看脚本输出
./deploy.sh 2>&1 | tee deploy.log

# 查看 Docker 日志
docker-compose logs
```

### 问题2：服务启动失败

**检查日志**：
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f blog-server
```

### 问题3：端口被占用

**检查端口占用**：
```bash
netstat -tlnp | grep -E '80|443|4000|5000|8000'
```

**修改端口**：编辑 `docker-compose.yml` 和 `.env` 文件

### 问题4：前端页面空白

**重新构建前端**：
```bash
# 停止服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

## 📋 服务器配置

- **服务器IP**: 212.64.28.65
- **MySQL端口**: 4000
- **Redis端口**: 5000
- **后端端口**: 8000
- **前端端口**: 80

## 📖 详细文档

- **完整部署指南**: [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)
- **自定义端口配置**: [CUSTOM_PORTS_CONFIG.md](./CUSTOM_PORTS_CONFIG.md)
