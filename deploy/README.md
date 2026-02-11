# 博客部署指南（小白版）

> 本指南面向**零基础用户**，手把手教你将博客部署到云服务器。
> 服务器系统：OpenCloudOS / CentOS / 其他 Linux
> 部署方式：Docker 容器化一键部署

---

## 目录

1. [整体架构说明](#1-整体架构说明)
2. [准备工作](#2-准备工作)
3. [第一步：连接服务器](#3-第一步连接服务器)
4. [第二步：上传项目到服务器](#4-第二步上传项目到服务器)
5. [第三步：配置环境变量](#5-第三步配置环境变量)
6. [第四步：一键部署](#6-第四步一键部署)
7. [第五步：验证部署成功](#7-第五步验证部署成功)
8. [常用运维命令](#8-常用运维命令)
9. [常见问题排查](#9-常见问题排查)
10. [更新部署](#10-更新部署)

---

## 1. 整体架构说明

部署后你的服务器上会运行 **4 个容器**（可以理解为 4 个独立的小服务器）：

```
用户浏览器
    │
    ▼ 访问 http://你的IP/
┌──────────────────────────────┐
│  Nginx 容器（端口 80）         │ ← 负责把网页展示给用户
│  - 博客前台: /                │
│  - 后台管理: /admin           │
│  - API 请求: /api/ → 转发后端  │
└──────────┬───────────────────┘
           │ 内部转发
           ▼
┌──────────────────────────────┐
│  Spring Boot 容器（端口 8000） │ ← 后端 API 服务
└──────┬──────────┬────────────┘
       │          │
       ▼          ▼
┌────────────┐  ┌─────────────┐
│ MySQL 容器  │  │ Redis 容器   │
│ (端口 3306) │  │ (端口 6379)  │
└────────────┘  └─────────────┘
```

**端口映射**（宿主机 → 容器内部）：

| 服务 | 宿主机端口 | 容器内部端口 | 说明 |
|------|-----------|------------|------|
| Nginx | 80 | 80 | 用户通过 80 端口访问网站 |
| MySQL | 4000 | 3306 | 你可以通过 4000 端口连接 MySQL |
| Redis | 5000 | 6379 | 你可以通过 5000 端口连接 Redis |
| Spring Boot | 8000 | 8000 | 后端 API 端口 |

---

## 2. 准备工作

在开始之前，你需要准备好：

- [x] **一台云服务器**（已有：212.64.28.65）
- [x] **服务器 root 密码**（登录用）
- [x] **项目代码已推送到 GitHub**
- [ ] **一个 SSH 工具**（Windows 推荐 [MobaXterm](https://mobaxterm.mobatek.net/) 或 系统自带的 PowerShell）
- [ ] **腾讯云安全组开放端口**（非常重要！见下方）

### 2.1 开放腾讯云安全组端口（必做！）

> **如果不做这一步，外网无法访问你的网站！**

1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 **云服务器** → 点击你的服务器实例
3. 点击 **安全组** 标签
4. 点击 **编辑规则** → **添加入站规则**，添加以下端口：

| 端口 | 用途 | 必须？ |
|------|------|--------|
| **80** | 网站访问 | **必须** |
| **443** | HTTPS（暂时不用） | 可选 |
| **8000** | 后端 API 直接访问 | 可选 |
| **4000** | MySQL 远程连接 | 可选（调试用） |
| **5000** | Redis 远程连接 | 可选（调试用） |

> 最少只需要开放 **80** 端口即可，因为 Nginx 会把 API 请求转发给后端。

---

## 3. 第一步：连接服务器

### 方法 A：使用 PowerShell（Windows 自带）

```powershell
ssh root@212.64.28.65
```

输入密码后回车（密码输入时不会显示，正常现象）。

### 方法 B：使用 MobaXterm

1. 打开 MobaXterm → 点击 Session → SSH
2. Remote host: `212.64.28.65`
3. Username: `root`
4. 点击 OK，输入密码

**看到类似 `[root@VM-0-15-opencloudos ~]#` 就表示连接成功了。**

---

## 4. 第二步：上传项目到服务器

### 方法 A：从 GitHub 拉取（推荐）

在服务器上执行：

```bash
# 1. 安装 git（如果没有）
dnf install -y git

# 2. 进入工作目录
cd /optcd /opt

# 3. 从 GitHub 克隆项目（替换成你自己的仓库地址）
git clone https://github.com/你的用户名/你的仓库名.git aricOnline

# 4. 进入项目的 deploy 目录
cd /opt/aricOnline/deploy
```

### 方法 B：使用 MobaXterm 直接上传

如果 git clone 太慢，可以：
1. 在本地把项目打包成 zip
2. 用 MobaXterm 左侧文件面板拖拽上传到 `/opt/`
3. 在服务器解压：`unzip 文件名.zip -d /opt/aricOnline`

**确认上传成功**（目录结构应该是这样）：

```bash
ls /opt/aricOnline/
# 应该看到：blog-springboot/  blog-vue/  deploy/  emoji/  ...
```

---

## 5. 第三步：配置环境变量

> 这一步是告诉程序你的密码、密钥等敏感信息。

```bash
# 1. 进入 deploy 目录
cd /opt/aricOnline/deploy

# 2. 从模板创建配置文件
cp .env.example .env

# 3. 编辑配置文件
vim .env
```

### vim 基本操作（不会用 vim 的看这里）

1. 按 `i` 进入编辑模式（左下角出现 `-- INSERT --`）
2. 用方向键移动光标，修改内容
3. 修改完按 `Esc` 退出编辑模式
4. 输入 `:wq` 然后回车 → 保存并退出
5. 如果改错了输入 `:q!` 回车 → 不保存退出

### 必须修改的配置项

打开 `.env` 后，你**必须修改**以下内容（其他保持默认即可）：

```bash
# -------- MySQL --------
MYSQL_PASSWORD=设置一个强密码    # 例如: MyBlog@2026

# -------- Redis --------
REDIS_PASSWORD=设置一个密码      # 例如: redis123

# -------- 博客地址 --------
BLOG_URL=http://212.64.28.65/   # 改成你的服务器 IP

# -------- 文件上传 --------
# 如果你有阿里云 OSS，填写以下信息：
UPLOAD_STRATEGY=oss
OSS_URL=https://你的bucket.oss-cn-beijing.aliyuncs.com/
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET=你的bucket名
OSS_ACCESS_KEY_ID=你的key
OSS_ACCESS_KEY_SECRET=你的secret

# 如果没有 OSS，改成 local（图片存服务器本地）：
UPLOAD_STRATEGY=local

# -------- AI 功能（可选）--------
AI_API_KEY=你的deepseek密钥     # 没有可以留空，AI 功能不可用而已
```

### 可选配置项

```bash
# -------- 邮箱（评论通知用）--------
MAIL_USERNAME=你的qq邮箱@qq.com
MAIL_PASSWORD=你的smtp授权码      # 不是QQ密码！是邮箱的授权码

# -------- 第三方登录（可选）--------
OAUTH_GITEE_CLIENT_ID=
OAUTH_GITHUB_CLIENT_ID=
```

---

## 6. 第四步：一键部署

> 这一步会自动安装 Docker、拉取镜像、构建并启动所有服务。

```bash
# 1. 确保在 deploy 目录下
cd /opt/aricOnline/deploy

# 2. 赋予脚本执行权限
chmod +x deploy.sh

# 3. 运行部署脚本
./deploy.sh
```

### 部署过程中会发生什么？

脚本会自动执行以下步骤（你只需要等待）：

| 步骤 | 做什么 | 大约耗时 |
|------|--------|---------|
| 1 | 检查/安装 Docker | 1-2 分钟 |
| 2 | 检查/安装 Docker Compose | 1 分钟 |
| 3 | 配置 Docker 镜像加速（国内下载更快） | 几秒 |
| 4 | 检查 .env 配置文件 | 几秒 |
| 5 | 拉取 MySQL、Redis 镜像 | 2-5 分钟 |
| 6 | **构建后端镜像**（Maven 下载依赖 + 编译） | **5-15 分钟** |
| 7 | **构建前端镜像**（npm install + build） | **5-10 分钟** |
| 8 | 启动所有容器 | 1-2 分钟 |
| 9 | 健康检查 | 1 分钟 |

**总共大约 15-30 分钟**，首次构建较慢是正常的（需要下载大量依赖）。

### 如果脚本中途提示 `.env` 不存在

说明你还没配置环境变量，回到[第三步](#5-第三步配置环境变量)。

### 如果脚本提示某个镜像拉取失败

网络问题，重新运行 `./deploy.sh` 即可（已下载的部分会缓存，不会重复下载）。

---

## 7. 第五步：验证部署成功

### 7.1 查看容器状态

```bash
cd /opt/aricOnline/deploy
docker-compose ps
```

**正常结果**（4 个容器都是 `Up` 状态）：

```
NAME          STATUS                    PORTS
blog-mysql    Up (healthy)              0.0.0.0:4000->3306/tcp
blog-redis    Up (healthy)              0.0.0.0:5000->6379/tcp
blog-server   Up (health: starting)     0.0.0.0:8000->8000/tcp
blog-nginx    Up                        0.0.0.0:80->80/tcp
```

> `health: starting` 表示后端还在启动中，等 1-2 分钟变成 `healthy` 就好了。

### 7.2 浏览器访问

打开浏览器访问：

| 地址 | 说明 |
|------|------|
| `http://212.64.28.65/` | 博客前台首页 |
| `http://212.64.28.65/admin` | 后台管理页面 |

**如果能看到页面，恭喜你部署成功了！**

### 7.3 如果页面打不开

按以下顺序排查：

```bash
# 1. 查看所有容器状态
docker-compose ps

# 2. 查看哪个容器有问题（看 STATUS 列是否有 Exit 或 Restarting）

# 3. 查看出问题的容器日志
docker-compose logs blog-server   # 后端日志
docker-compose logs nginx          # 前端日志
docker-compose logs mysql          # 数据库日志
docker-compose logs redis          # Redis 日志
```

---

## 8. 常用运维命令

以下命令都在 `/opt/aricOnline/deploy` 目录下执行：

### 查看状态

```bash
docker-compose ps              # 查看容器状态
docker-compose logs -f         # 实时查看所有日志（Ctrl+C 退出）
docker-compose logs -f blog-server  # 只看后端日志
```

### 重启服务

```bash
docker-compose restart              # 重启所有服务
docker-compose restart blog-server  # 只重启后端
docker-compose restart nginx        # 只重启前端
```

### 停止/启动

```bash
docker-compose down            # 停止并删除所有容器（数据不会丢失）
docker-compose up -d           # 重新启动（不重新构建）
docker-compose up -d --build   # 重新构建并启动（代码有更新时用）
```

### 进入数据库

```bash
# 进入 MySQL 命令行
docker exec -it blog-mysql mysql -uroot -p
# 输入你在 .env 中设置的 MYSQL_PASSWORD

# 常用 SQL
show databases;
use blog;
show tables;
```

### 清理空间

```bash
docker system prune -a --volumes  # 清理所有未使用的镜像和卷（谨慎使用！）
docker image prune               # 只清理悬空镜像
```

---

## 9. 常见问题排查

### Q1: 访问网站显示 "无法访问此网站"

**原因**: 安全组端口未开放。

**解决**: 去腾讯云控制台 → 安全组 → 开放 80 端口。参考[第 2.1 节](#21-开放腾讯云安全组端口必做)。

### Q2: 后端容器一直 Restarting

**排查**:
```bash
docker-compose logs blog-server | tail -50
```

常见原因：
- `.env` 中 `MYSQL_PASSWORD` 没填或填错
- MySQL 还没启动完就连接了 → 等 1 分钟后 `docker-compose restart blog-server`

### Q3: 前台能访问但 API 报错

**排查**:
```bash
# 检查后端是否正常运行
curl http://localhost:8000/
# 检查 Nginx 转发是否正常
curl http://localhost/api/
```

### Q4: Docker 镜像拉取超时

**解决**:
```bash
# 手动配置镜像加速
cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF
systemctl daemon-reload
systemctl restart docker

# 然后重新运行
./deploy.sh
```

### Q5: 构建前端报错 "Cannot read properties of undefined"

**原因**: 前端环境变量缺失。已在 Dockerfile.nginx 中自动处理，正常不会出现。

如果还出现，手动检查 `blog-vue/shoka-blog/.env.prod` 是否存在且包含所有 `VITE_` 变量。

### Q6: 数据库初始化失败

**排查**:
```bash
docker-compose logs mysql | tail -30
```

如果看到 "already initialized"，说明之前已经初始化过了。如果要重新初始化：
```bash
docker-compose down
docker volume rm deploy_mysql-data   # 删除 MySQL 数据卷
docker-compose up -d --build         # 重新构建启动
```

---

## 10. 更新部署

当你本地代码更新并 push 到 GitHub 后，在服务器执行：

```bash
# 1. 进入项目目录
cd /opt/aricOnline

# 2. 拉取最新代码
git pull origin master

# 3. 进入 deploy 目录
cd deploy

# 4. 重新构建并启动（只重建有变化的部分）
docker-compose up -d --build

# 5. 查看状态
docker-compose ps
```

> 第二次构建会快很多（Docker 有缓存），通常 3-5 分钟。

---

## deploy/ 目录文件说明

| 文件 | 作用 | 你需要动它吗？ |
|------|------|--------------|
| `.env.example` | 环境变量模板 | **需要**：复制为 `.env` 并填写 |
| `deploy.sh` | 一键部署脚本 | 不用动，直接运行 |
| `docker-compose.yml` | Docker 服务编排 | 不用动 |
| `Dockerfile` | 后端构建配置 | 不用动 |
| `Dockerfile.nginx` | 前端构建配置 | 不用动 |
| `nginx.conf` | Nginx 配置 | 不用动（除非要配 HTTPS） |
| `blog.sql` | 数据库初始化脚本 | 不用动（首次启动自动导入） |
| `mapping.json` | Elasticsearch 索引（可选） | 不用管（默认用 MySQL 搜索） |

---

## 快速命令速查表

```bash
# ===== 部署 =====
cd /opt/aricOnline/deploy    # 进入部署目录
cp .env.example .env          # 创建配置文件
vim .env                      # 编辑配置
chmod +x deploy.sh            # 赋予权限
./deploy.sh                   # 一键部署

# ===== 日常 =====
docker-compose ps              # 查看状态
docker-compose logs -f         # 查看日志
docker-compose restart         # 重启全部
docker-compose down            # 停止全部
docker-compose up -d --build   # 重建启动

# ===== 更新 =====
cd /opt/aricOnline && git pull && cd deploy && docker-compose up -d --build
```
