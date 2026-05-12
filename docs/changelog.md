# 变更日志

## 记录格式

```md
## YYYY-MM-DD - 标题

- 类型：
- 需求文档：
- 影响范围：
- 主要改动：
- 验证方式：
- 回滚说明：
```

## 未归档变更

## 2026-05-12 - 优化首页文章预览卡片信息布局

- 类型：视觉 / 前台展示
- 需求文档：`docs/requirements/2026-05-12-home-article-card-preview.md`
- 影响范围：首页最新文章区块、首页分类文章区块
- 主要改动：将置顶标识从标题内移到封面左上角并使用与发布时间一致的半透明深色背景、橙黄色文字，标题调整为两行省略并降低字号，新增 `articleDesc` 摘要展示，并将分类与标签拆成上下两行
- 验证方式：执行 `npm run typecheck` 检查前台类型；人工检查首页卡片在桌面、平板、手机布局下的置顶标签、标题省略、摘要和分类标签分行效果
- 回滚说明：恢复 `LatestArticlesSection.vue`、`CategorySections.vue` 中本次模板与样式改动，并移除本条日志和对应需求文档

## 2026-05-12 - 补充 VS Code Maven 调试配置

- 类型：开发工具 / 配置
- 需求文档：`docs/requirements/2026-05-12-vscode-maven-debug.md`
- 影响范围：VS Code 调试、Maven 依赖下载、本地后端启动参数
- 主要改动：新增 `.vscode/tasks.json`，固定使用 `D:\app\tool\maven\apache-maven-3.9.14\conf\settings.xml` 和 `D:\app\tool\maven\mvn_repo` 进行编译预热；新增 `.vscode/launch.json`，直接调试 `com.ican.BlogApplication`，并通过 JVM 参数覆盖 `deploy/.env` 中用于 Docker 的主机名，让本地调试改连远端 `212.64.28.65:4000/5000`
- 验证方式：已执行指定 Maven 配置下的 `clean compile -DskipTests`，编译通过；`spring-boot:run` 当前报错已定位为 `deploy/.env` 里的 Docker 内部主机名未被本地调试环境正确覆盖
- 回滚说明：删除 `.vscode/launch.json`、`.vscode/tasks.json`，并移除本条日志

## 2026-05-10 - 统一 Todo 效率工具卡片底色透明度

- 类型：视觉 / 修复
- 需求文档：`docs/requirements/2026-05-10-theme-contrast-optimization.md`
- 影响范围：Todo 任务池、习惯追踪统计卡片、习惯卡片
- 主要改动：将任务池和习惯追踪卡片统一回同一档 `--todo-card-bg` 底色，避免同一效率工具内出现两套不同透明度的卡片层级
- 验证方式：复查 Todo 页面任务池与习惯追踪在 gradient 主题下的卡片底色一致性
- 回滚说明：恢复 `TaskPool.vue`、`HabitView.vue` 中对 `--todo-card-bg` 的统一引用

## 2026-05-10 - 优化 Todo 工具卡片磨砂感与热力图对齐

- 类型：视觉 / 修复
- 需求文档：`docs/requirements/2026-05-10-theme-contrast-optimization.md`
- 影响范围：Todo 未分配任务卡片、习惯统计卡片、习惯年度热力图
- 主要改动：新增 Todo 软磨砂卡片变量，降低未分配任务卡片与习惯统计卡片白底强度；年度热力图补齐年初/年末占位格，并让月份文本按热力图周列定位
- 验证方式：运行 `npm run typecheck` 检查前端类型
- 回滚说明：恢复 `TaskPool.vue`、`HabitView.vue` 与 `theme-shoka.scss` 中本次 Todo 样式和热力图计算改动

## 2026-05-10 - 回退部分共用卡片的边框与底色增强

- 类型：视觉
- 需求文档：`docs/requirements/2026-05-10-theme-contrast-optimization.md`
- 影响范围：搜索页 AI 卡、首页推荐、每日推荐、聊天室
- 主要改动：将部分共用卡片恢复为原有无边框或弱边框表现，仅保留主题背景层次优化
- 验证方式：检查相关组件不再使用新增的卡片边框和面板底色
- 回滚说明：重新恢复这些组件的边框和底色增强样式

## 2026-05-10 - 优化全站主题背景与卡片对比度

- 类型：视觉
- 需求文档：`docs/requirements/2026-05-10-theme-contrast-optimization.md`
- 影响范围：前台全站主题、公共卡片、搜索页、首页、效率工具页、共用浮层
- 主要改动：新增主题对比度优化需求文档，统一 light / dark 的背景、卡片、阴影与边框变量，并调整首页推荐、搜索页 AI 卡、每日推荐、聊天室等共用组件的背景层次
- 验证方式：检查主题变量与共用组件样式是否统一，确认没有继续使用旧的灰阶背景作为主底色
- 回滚说明：恢复 `theme-shoka.scss`、`common.scss`、`index.scss` 及相关共用组件的样式改动

## 2026-05-10 - 补充中文 Git 提交文案规范

- 类型：文档
- 需求文档：`docs/requirements/2026-05-10-search-page.md`
- 影响范围：仓库协作规范、任务交付格式
- 主要改动：在 `AGENTS.md` 中补充每次任务完成后必须提供中文 Git 提交文案的要求，并约定 Conventional Commits 风格
- 验证方式：检查 `AGENTS.md` 新增交付要求段落
- 回滚说明：删除 `AGENTS.md` 中新增的提交文案规范段落

## 2026-05-10 - 补充开发快速入门与搜索页需求文档

- 类型：文档
- 需求文档：`docs/requirements/2026-05-10-search-page.md`
- 影响范围：文档中心、搜索页认知路径、前台搜索链路说明
- 主要改动：新增开发快速入门文档，新增搜索页需求文档，并把入口加入 `docs/README.md`
- 验证方式：检查文档文件存在、索引链接正确、搜索页链路说明覆盖顶部搜索、分类、标签和 AI 对话入口
- 回滚说明：删除新增文档并移除 `docs/README.md` 中对应索引项
