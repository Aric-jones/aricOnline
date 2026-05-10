# 搜索页需求文档

## 基本信息

- 需求编号：REQ-2026-05-10-SEARCH-001
- 需求名称：搜索页行为说明与文档补全
- 提出时间：2026-05-10
- 负责人：Codex
- 状态：已完成

## 背景

仓库需要一份可直接指导阅读和维护的搜索页需求文档，避免后续查看 `blog-vue/shoka-blog/src/views/Search/index.vue` 时依赖上下文猜测。

## 目标

- 明确搜索页支持的查询类型。
- 明确搜索页与顶部导航、文章接口、分类接口、标签接口的关系。
- 明确搜索页的展示规则、分页规则和 AI 对话入口。

## 非目标

- 不调整搜索页 UI。
- 不修改后端接口返回结构。
- 不新增搜索算法或索引方案。

## 功能说明

搜索页通过路由 query 决定展示内容：

- `keyword`：关键词搜索结果。
- `categoryId` / `categoryName`：分类文章列表。
- `tagId` / `tagName`：标签文章列表。

关键词来源于顶部导航搜索框，搜索后跳转到 `/search?keyword=...`。
搜索页在关键词模式下调用文章搜索接口，并在关键词存在时展示 AI 对话组件。
分类和标签模式下，页面分别请求分类/标签文章列表，并补回分类名、标签名和文章总数。

## 影响范围

| 类型 | 说明 |
| --- | --- |
| 前台页面 | `blog-vue/shoka-blog/src/views/Search/index.vue` |
| 前台组件 | `blog-vue/shoka-blog/src/components/Layout/Header/NavBar.vue`、`blog-vue/shoka-blog/src/views/Search/AiChat.vue` |
| 前台接口 | `blog-vue/shoka-blog/src/api/article/index.ts`、`blog-vue/shoka-blog/src/api/category`、`blog-vue/shoka-blog/src/api/tag` |
| 路由 | `/search` 页面及其 query 规则 |
| 存储 | 搜索历史使用本地存储 `search_history` |

## 受影响文件

- `blog-vue/shoka-blog/src/views/Search/index.vue`
- `blog-vue/shoka-blog/src/views/Search/AiChat.vue`
- `blog-vue/shoka-blog/src/components/Layout/Header/NavBar.vue`
- `blog-vue/shoka-blog/src/api/article/index.ts`
- `blog-vue/shoka-blog/src/api/article/types.ts`

## 验收标准

- 输入关键词后能跳转到搜索页并显示结果。
- 分类和标签链接能正确切换到对应筛选结果。
- 搜索历史能保存、排序和删除。
- 关键词模式下 AI 对话入口可见。

## 测试计划

- 手动验证顶部导航搜索跳转。
- 手动验证关键词、分类、标签三种 query 场景。
- 手动验证搜索历史的新增、置顶、删除、清空。

## 风险与回滚

- 风险：搜索页 query 约定不一致会导致页面无法正确识别模式。
- 风险：后端接口返回结构变化会影响列表渲染和名称补全。
- 回滚：恢复 `Search/index.vue`、`NavBar.vue` 和相关接口调用逻辑到修改前版本。

