# 博客项目

基于 **Spring Boot** 与 **Vue 3** 的前后端分离博客系统，前台参考 Hexo Shoka 主题风格，后台基于若依改造。

## 项目结构

```
blog/
├── blog-springboot/     # 后端：Spring Boot + MyBatis-Plus + Redis + Sa-Token 等
├── blog-vue/
│   ├── shoka-blog/      # 前台：Vue3 + Pinia + TypeScript + Vite
│   └── shoka-admin/     # 后台管理：Vue3 + Element Plus + Vite
├── deploy/              # Docker / 部署相关
├── blog.sql             # 数据库初始化脚本
└── README.md
```

## 技术栈

| 端   | 技术 |
|------|------|
| 前台 | Vue 3、Pinia、Vue Router、TypeScript、Vite、Naive UI、ECharts、Swiper |
| 后台 | Vue 3、Element Plus、Vite、TypeScript |
| 后端 | Spring Boot 2.6、MySQL 8、Redis、MyBatis-Plus、Sa-Token、Swagger、Elasticsearch、RabbitMQ 等 |

## 环境要求

- **JDK** 11+
- **Node.js** 16+（推荐 18+）
- **MySQL** 8.0+
- **Redis** 6+
- 可选：Elasticsearch 7.x、RabbitMQ（搜索与消息队列）

## 本地运行

### 1. 数据库

- 创建数据库，执行根目录下 `blog.sql` 导入表结构与初始数据。
- 在后端配置中填写数据库连接（见 `blog-springboot` 配置文件）。

### 2. 后端

```bash
cd blog-springboot
# 修改 application*.yml 中数据库、Redis 等配置
mvn spring-boot:run
```

默认会提供接口（如 `http://localhost:8080`），Swagger 文档一般为 `/api/doc.html`（以实际配置为准）。

### 3. 前台（博客）

```bash
cd blog-vue/shoka-blog
npm install   # 或 pnpm install
npm run dev
```

在 `src/utils/request.ts` 或环境变量中配置后端接口地址（如 `VITE_APP_BASE_API`）。

### 4. 后台管理

```bash
cd blog-vue/shoka-admin
npm install
npm run dev
```

同样需配置后端接口地址；本地运行时可使用管理员账号登录（账号见数据库或默认说明）。

### 5. 注意事项

- 若使用 `utils/token.ts` 等处的 `domain` 配置，本地开发时可去掉或改为当前域名，避免 Cookie 等问题。
- 第三方登录（QQ、Gitee、GitHub）、OSS/COS 等需在后台或配置中自行填写并开通。

## 主要功能

- **前台**：首页按分类展示文章、归档、分类/标签、说说、相册、友链、留言板、关于、搜索、评论、音乐播放器、聊天室等。
- **后台**：文章/分类/标签/友链管理、评论与留言审核、用户与权限、系统配置、日志与定时任务等。
- **部署**：支持 Docker Compose 一键部署（见 `deploy/` 目录）。

## 首页展示说明

首页按**分类**分组展示文章：

- 每个分类一块区域，左上为分类名称，右上为「更多」链接到该分类列表页。
- 每个分类最多展示 6 篇文章；点击「更多」进入该分类的分页列表。

## 构建与部署

**前台构建：**

```bash
cd blog-vue/shoka-blog
npm run build
```

**后台构建：**

```bash
cd blog-vue/shoka-admin
npm run build
```

构建产物一般在各项目的 `dist` 目录，可交由 Nginx 或其他 Web 服务器托管；后端可打为 jar 运行或放入 Docker（参考 `deploy/`）。

## License

见项目根目录 `LICENSE` 文件。
