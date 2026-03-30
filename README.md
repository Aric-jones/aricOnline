# Aric's Blog

基于 **Spring Boot 2.6** 与 **Vue 3** 的前后端分离博客系统，前台参考 Hexo Shoka 主题风格，后台基于若依改造。支持 **Docker Compose 一键部署**，内置多主题切换、效率工具、AI 助手等特色功能。

## 项目结构

```
blog/
├── blog-springboot/          # 后端 Spring Boot 项目
│   ├── src/main/java/        # Java 源码
│   ├── src/main/resources/   # 配置文件
│   └── pom.xml               # Maven 依赖
├── blog-vue/
│   ├── shoka-blog/           # 前台博客（Vue 3 + Naive UI + Vite 5）
│   └── shoka-admin/          # 后台管理（Vue 3 + Element Plus + Vite）
├── deploy/                   # Docker 部署相关
│   ├── docker-compose.yml    # 容器编排（MySQL + Redis + 后端 + Nginx）
│   ├── Dockerfile            # 后端多阶段构建
│   ├── Dockerfile.nginx      # 前端多阶段构建
│   ├── nginx.conf            # Nginx 配置（反向代理 + SSL）
│   ├── deploy.sh             # 一键部署脚本（含 Docker 安装）
│   ├── update.sh             # 热更新脚本（独立更新前后端）
│   ├── blog.sql              # 数据库初始化脚本
│   └── .env.example          # 环境变量模板
└── README.md
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前台** | Vue 3、Pinia、Vue Router 4、TypeScript、Vite 5、Naive UI、ECharts、Swiper | Vue 3.4、Vite 5.2 |
| **后台管理** | Vue 3、Element Plus、Vite、TypeScript、Pinia | Vue 3.2、Element Plus 2.3 |
| **后端** | Spring Boot、MyBatis-Plus、MySQL、Redis、Sa-Token、Knife4j (Swagger) | Spring Boot 2.6.14、Java 11 |
| **存储** | 阿里云 OSS / 腾讯 COS / 七牛云 / 本地存储 | 可选 |
| **搜索** | MySQL LIKE / Elasticsearch | ES 7.x 可选 |
| **部署** | Docker Compose、Nginx、多阶段构建 | — |
| **AI** | DeepSeek / 兼容 OpenAI API 的大模型 | 可选 |

## 功能介绍

### 博客前台

| 模块 | 功能说明 |
|------|---------|
| **首页** | 按分类分组展示文章（每类最多 6 篇），打字机效果，轮播图 |
| **文章** | Markdown 渲染、代码高亮、目录导航、上下篇、阅读量统计 |
| **归档** | 时间线展示全部文章 |
| **分类 / 标签** | 可视化图表展示，点击跳转对应文章列表 |
| **搜索** | 关键词搜索文章（支持 MySQL / Elasticsearch），搜索历史记录 |
| **说说** | 朋友圈式动态，支持图片、点赞、评论 |
| **相册 / 图床** | 相册浏览，图片上传管理 |
| **友链** | 友情链接展示与申请 |
| **留言板** | 弹幕式留言展示 |
| **评论** | 多级评论、表情包、@提及 |
| **用户** | 注册登录、第三方登录（QQ / Gitee / GitHub）、个人中心 |
| **主题切换** | 三套主题：白天（Light）、黑夜（Dark / 极光风格）、渐变（Gradient），各页面标题跟随主题变色 |
| **音乐播放器** | 全局 APlayer，支持歌单 |
| **关于** | 站长介绍页 |

### 效率工具（登录后可用）

| 模块 | 功能说明 |
|------|---------|
| **代办列表** | 任务增删改查、优先级、分类、状态切换、分页 |
| **任务池** | 未分配任务管理，拖拽分配到周计划 |
| **日历视图** | 日历展示待办，按日期查看和管理 |
| **甘特图** | 任务时间跨度可视化 |
| **日记** | 每日日记记录，支持心情标签 |
| **思考沉淀** | 思考笔记管理，**支持分类筛选**，按分类快速定位 |
| **时间管理** | 时间块记录，常用事件快速填充，分类统计图表 |
| **习惯追踪** | 习惯打卡，可视化追踪 |
| **AI 助手** | 基于大模型的智能总结、建议、对话 |
| **主题色切换** | 紫 / 蓝 / 绿三套主题色，一键切换 |

### 后台管理

| 模块 | 功能说明 |
|------|---------|
| **文章管理** | 新增 / 编辑 / 删除 / 置顶 / 推荐，Markdown 编辑器 |
| **分类 / 标签** | 增删改查 |
| **评论 / 留言** | 审核、删除、回复 |
| **用户管理** | 用户列表、角色、权限分配 |
| **友链管理** | 审核友链申请 |
| **说说管理** | 发布、编辑、删除 |
| **相册管理** | 相册与照片管理 |
| **站点配置** | 站点名称、公告、社交链接、SEO 等 |
| **日志** | 操作日志、登录日志、定时任务日志 |
| **定时任务** | Quartz 调度，支持 CRON 表达式 |

## 环境要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 11+ | 后端编译运行 |
| Node.js | 16+（推荐 18） | 前端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6+ | 缓存 / 会话 |
| Docker | 20.10+ | 部署（可选） |
| Elasticsearch | 7.x | 搜索（可选，默认用 MySQL） |

## 本地开发

### 1. 数据库初始化

```bash
# 创建数据库并导入初始数据
mysql -u root -p -e "CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -u root -p blog < deploy/blog.sql
```

### 2. 启动后端

```bash
cd blog-springboot
# 方式一：使用 .env 文件（推荐，项目已配置读取 .env）
cp .env.example .env
# 编辑 .env 填写 MySQL、Redis 密码等
# 然后启动
mvn spring-boot:run

# 方式二：直接修改 application-dev.yml
mvn spring-boot:run
```

启动后访问 API 文档：`http://localhost:9000/doc.html`

### 3. 启动前台

```bash
cd blog-vue/shoka-blog
npm install
npm run dev
```

### 4. 启动后台管理

```bash
cd blog-vue/shoka-admin
npm install
npm run dev
```

### 5. 注意事项

- 本地开发时，前台默认代理 API 到 `localhost:9000`，无需额外配置
- 第三方登录（QQ / Gitee / GitHub）需在后台系统配置中填写 Key/Secret
- 文件上传默认为本地存储，如需 OSS 请在 `.env` 或后台配置中设置

## Docker 一键部署

### 架构说明

```
用户浏览器 → Nginx(:80/:443)
               ├── /       → 博客前台（静态文件）
               ├── /admin  → 后台管理（静态文件）
               └── /api    → Spring Boot(:9000) 反向代理
                                ├── MySQL(:3306)
                                └── Redis(:6379)
```

四个容器：`blog-mysql`、`blog-redis`、`blog-server`（后端）、`blog-nginx`（前端 + 反向代理）

### 部署步骤

#### 1. 上传项目到服务器

```bash
# Git 克隆
cd /opt && git clone <仓库地址> aricOnline

# 或打包上传
tar --exclude='node_modules' --exclude='target' -czf blog.tar.gz blog/
scp blog.tar.gz root@你的服务器:/opt/
ssh root@你的服务器 "cd /opt && tar xzf blog.tar.gz && mv blog aricOnline"
```

#### 2. 配置环境变量

```bash
cd /opt/aricOnline/deploy
cp .env.example .env
vim .env
```

**必须配置的项：**

```env
MYSQL_PASSWORD=你的MySQL密码    # 必改
REDIS_PASSWORD=你的Redis密码    # 必改
BLOG_URL=https://你的域名/      # 必改
UPLOAD_STRATEGY=local           # local / oss / cos
```

#### 3. 放置 SSL 证书（HTTPS）

```bash
cp /你的证书路径/域名.pem  /opt/aricOnline/deploy/
cp /你的证书路径/域名.key  /opt/aricOnline/deploy/
```

> 如不需要 HTTPS，修改 `nginx.conf` 去掉 SSL 相关配置即可。

#### 4. 一键部署

```bash
cd /opt/aricOnline/deploy

# 方式一：使用部署脚本（首次推荐，自动安装 Docker）
chmod +x deploy.sh
./deploy.sh

# 方式二：已有 Docker，直接启动
docker compose up -d --build
```

首次构建约 **5-10 分钟**（下载依赖 + 编译），后续有缓存会快很多。

#### 5. 验证

```bash
docker compose ps              # 4 个容器都应为 Up
docker compose logs -f         # 查看全部日志
```

访问地址：
- 博客前台：`https://你的域名/`
- 后台管理：`https://你的域名/admin`
- API 文档：`https://你的域名/api/doc.html`

## 日常更新

初始化部署后，**日常更新代码不需要重启数据库**，使用 `--no-deps` 独立更新：

```bash
cd /opt/aricOnline/deploy

# 仅更新后端（MySQL/Redis 不受影响，停机约 30s）
git pull && docker compose up -d --build --no-deps blog-server

# 仅更新前端（后端/数据库不受影响，停机约 10s）
git pull && docker compose up -d --build --no-deps nginx

# 前后端一起更新（数据库不受影响）
git pull && docker compose up -d --build --no-deps blog-server nginx
```

也可以使用交互式热更新脚本：

```bash
chmod +x update.sh
./update.sh          # 交互式菜单
./update.sh backend  # 仅更新后端
./update.sh frontend # 仅更新前端
./update.sh all      # 前后端一起
```

## 常用运维命令

```bash
# 状态查看
docker compose ps                    # 容器状态
docker compose logs -f blog-server   # 后端日志
docker compose logs -f nginx         # Nginx 日志

# 启停
docker compose restart blog-server   # 重启后端（改了 .env 后用）
docker compose restart nginx         # 重启 Nginx
docker compose down                  # 停止全部（数据卷保留）

# 进入容器
docker exec -it blog-mysql mysql -uroot -p   # 进入 MySQL
docker exec -it blog-redis redis-cli          # 进入 Redis
docker exec -it blog-server /bin/bash         # 进入后端

# 清理
docker image prune -f                # 清理旧镜像释放磁盘
```

## License

见项目根目录 `LICENSE` 文件。
