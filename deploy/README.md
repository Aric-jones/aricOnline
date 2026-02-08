# 博客部署指南

## 项目架构

```
┌──────────┐    80/443     ┌──────────┐    8080     ┌──────────────┐
│  浏览器   │ ──────────→  │  Nginx   │ ──────────→ │ Spring Boot  │
│          │              │ 前台+后台 │   /api/     │   后端 API    │
└──────────┘              └──────────┘              └──────┬───────┘
                                                          │
                                              ┌───────────┼───────────┐
                                              ↓           ↓           ↓
                                         ┌────────┐ ┌─────────┐ ┌─────────┐
                                         │ MySQL  │ │  Redis  │ │ OSS/本地 │
                                         └────────┘ └─────────┘ └─────────┘
```

**4 个 Docker 容器**：Nginx（前端）、Spring Boot（后端）、MySQL、Redis

---

## 一、一键部署（推荐）

### 前提条件

- 服务器安装了 Docker 和 Docker Compose
- 2 核 2G 以上

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/your-repo/blog.git
cd blog/deploy

# 2. 创建环境变量文件
cp .env.example .env

# 3. 编辑 .env，填入真实密码和密钥
vim .env

# 4. 一键启动！
sh blog-start.sh
```

首次启动会自动：
- 构建后端 JAR（Maven 多阶段构建，无需本地安装 Maven）
- 构建前端静态文件（Node.js 多阶段构建，无需本地安装 Node）
- 启动 MySQL 并自动导入 `blog.sql`
- 启动 Redis
- 启动后端服务
- 启动 Nginx 托管前端

**启动完成后：**
| 服务 | 地址 |
|------|------|
| 博客前台 | http://你的IP |
| 后台管理 | http://你的IP/admin |
| 后端 API | http://你的IP:8080 |

---

## 二、常用运维命令

```bash
cd blog/deploy

# 查看容器状态
sh blog-start.sh status

# 查看实时日志
sh blog-start.sh logs

# 停止所有服务
sh blog-start.sh down

# 重启（重新构建）
sh blog-start.sh restart

# 单独重启某个服务
docker-compose restart blog-server

# 进入 MySQL
docker exec -it blog-mysql mysql -uroot -p

# 进入 Redis
docker exec -it blog-redis redis-cli -a your_password
```

---

## 三、环境变量说明

所有敏感配置通过 `.env` 文件注入，**不会提交到 Git**。

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_PASSWORD` | MySQL root 密码 | *(必填)* |
| `REDIS_PASSWORD` | Redis 密码 | *(必填)* |
| `MAIL_USERNAME` | SMTP 邮箱 | *(必填)* |
| `MAIL_PASSWORD` | SMTP 授权码 | *(必填)* |
| `OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey | *(使用 OSS 时必填)* |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS Secret | *(使用 OSS 时必填)* |
| `AI_API_KEY` | AI 服务 API Key | *(使用 AI 时必填)* |
| `UPLOAD_STRATEGY` | 上传策略 | `oss` |
| `SEARCH_MODE` | 搜索模式 | `mysql` |

完整变量列表见 `.env.example`。

---

## 四、HTTPS 配置（可选）

1. 申请 SSL 证书（阿里云/腾讯云免费证书）

2. 上传证书到服务器 `/etc/ssl/certs/` 目录

3. 编辑 `deploy/nginx.conf`，取消 HTTPS server 块的注释，填入：
   - `server_name`：你的域名
   - `ssl_certificate`：证书路径
   - `ssl_certificate_key`：密钥路径

4. 重启 Nginx：
   ```bash
   docker-compose restart nginx
   ```

---

## 五、手动部署（不用 Docker）

如果不想用 Docker，也可以手动部署：

### 后端

```bash
cd blog-springboot

# 设置环境变量（Linux）
export MYSQL_PASSWORD=your_password
export REDIS_PASSWORD=your_password
# ... 其他变量参考 .env.example

# 打包
mvn package -DskipTests

# 运行
java -jar target/blog-springboot-2.0.jar
```

### 前端

```bash
# 博客前台
cd blog-vue/shoka-blog
npm install
npm run build
# 产物在 blog/ 目录

# 后台管理
cd blog-vue/shoka-admin
npm install
npm run build
# 产物在 dist/ 目录
```

将前端产物部署到 Nginx，配置反向代理指向后端 8080 端口。

---

## 六、项目目录结构

```
blog/
├── blog-springboot/           # 后端 Spring Boot
│   ├── src/
│   │   └── main/resources/
│   │       ├── application.yml       # 入口（选择 profile）
│   │       └── application-dev.yml   # 开发/部署配置（使用环境变量）
│   └── pom.xml
├── blog-vue/
│   ├── shoka-blog/            # 博客前台 Vue3
│   └── shoka-admin/           # 后台管理 Vue3
├── deploy/                    # 部署文件
│   ├── .env.example           # 环境变量模板（⭐ 复制为 .env）
│   ├── docker-compose.yml     # Docker 编排
│   ├── Dockerfile             # 后端镜像
│   ├── Dockerfile.nginx       # 前端 Nginx 镜像
│   ├── nginx.conf             # Nginx 配置
│   ├── blog-start.sh          # 一键部署脚本
│   └── README.md              # 本文档
├── blog.sql                   # 数据库初始化 SQL
├── .gitignore                 # Git 忽略规则
└── README.md                  # 项目说明
```

---

## 七、常见问题

### Q: 首次启动后端报数据库连接失败？
MySQL 容器首次启动需要初始化数据库，可能需要 30-60 秒。`docker-compose.yml` 已配置 `depends_on` + `healthcheck`，后端会等 MySQL 就绪后才启动。如果仍然失败，手动重启后端：
```bash
docker-compose restart blog-server
```

### Q: 如何更新代码？
```bash
git pull
cd deploy
sh blog-start.sh restart
```

### Q: 数据会丢失吗？
不会。MySQL 和 Redis 数据都挂载了 Docker Volume，即使容器重建数据也不会丢失。

### Q: 如何备份数据库？
```bash
docker exec blog-mysql mysqldump -uroot -p$MYSQL_PASSWORD blog > backup.sql
```
