# 开发快速入门

这份文档用于让新读者在尽量少读代码的前提下，快速建立仓库全局认知。

## 项目一句话

Aric's Blog 是一个前后端分离的个人博客与内容管理系统，覆盖前台展示、后台管理、后端服务和部署运维。

## 顶层模块

| 模块 | 路径 | 职责 |
| --- | --- | --- |
| 博客前台 | `blog-vue/shoka-blog` | 博客展示、互动、搜索、效率工具、AI 助手 |
| 后台管理 | `blog-vue/shoka-admin` | 内容管理、系统管理、日志、任务、监控 |
| 后端服务 | `blog-springboot` | 业务 API、认证授权、数据访问、文件、日志、任务 |
| 部署配置 | `deploy` | 容器编排、反代、初始化、升级脚本 |

## 核心阅读顺序

1. `docs/project-overview.md`
2. `docs/modules.md`
3. `docs/requirements/*.md`
4. 对应模块的 README
5. 具体页面或接口代码

## 关键理解点

- 前台与后台是独立应用，入口和职责分开。
- 搜索、文章、评论、登录、效率工具等能力会跨前台和后端联动。
- 路由、权限、接口、数据库、配置项一旦变动，都必须先补需求文档。
- 完成修改后要补 `docs/changelog.md`。

## 常见改动入口

| 目标 | 优先查看 |
| --- | --- |
| 前台页面 | `blog-vue/shoka-blog/src/views` |
| 前台公共组件 | `blog-vue/shoka-blog/src/components` |
| 前台路由 | `blog-vue/shoka-blog/src/router` |
| 前台接口 | `blog-vue/shoka-blog/src/api` |
| 后台页面 | `blog-vue/shoka-admin/src/views` |
| 后台接口 | `blog-vue/shoka-admin/src/api` |
| 后端分层 | `blog-springboot/src/main/java` |
| 部署脚本 | `deploy` |

## 搜索页链路

- 顶部导航输入关键词后跳转到 `/search?keyword=...`
- 搜索页根据 query 参数决定展示关键词搜索、分类结果或标签结果
- 关键词搜索会调用后端文章搜索接口
- 搜索页会根据关键词展示 AI 对话入口
- 分类和标签结果会分别回查分类/标签接口补全名称和数量

## 文档约定

- 新需求先写 `docs/requirements/` 下的文档，再改代码。
- 变更完成后补 `docs/changelog.md`。
- 如果影响模块职责，更新 `docs/modules.md`。
- 如果影响产品范围，更新 `docs/project-overview.md`。

