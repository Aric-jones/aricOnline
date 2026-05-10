# 主题对比度优化需求文档

## 基本信息

- 需求编号：REQ-2026-05-10-THEME-001
- 需求名称：全站主题对比度优化
- 提出时间：2026-05-10
- 负责人：Codex
- 状态：进行中

## 背景

当前前台在白天和黑夜两套主题下，页面背景和卡片背景的层次区分不够明显，首页、搜索页、效率工具页等共用区域的视觉分层不稳定。

## 目标

- 提升页面背景与卡片的对比度。
- 统一 light / dark 两套主题下的背景、卡片、阴影表现。
- 让首页、搜索页、效率工具页、文章页和侧边栏具备一致的层次感。

## 非目标

- 不修改业务逻辑。
- 不调整页面结构。
- 不重做主题配色体系，只做层次和可读性优化。

## 功能说明

本次优化主要通过主题变量和少量公共样式调整实现：

- 调整全局背景色与卡片底色的关系。
- 强化通用卡片阴影、边框和悬浮层次。
- 对仍存在硬编码背景的共用组件做局部覆盖。
- 保持 light / dark 两套主题各自的风格延续。

## 影响范围

| 类型 | 说明 |
| --- | --- |
| 前台页面 | 首页、搜索页、文章页、归档页、分类页、标签页、效率工具页、留言等共用前台页面 |
| 前台组件 | 首页推荐/最新文章/说说、搜索页 AI 卡、侧边栏、每日推荐弹窗、通用卡片组件 |
| 样式变量 | `theme-shoka.scss` 中的全局背景、卡片、阴影、边框变量 |
| 公共样式 | `common.scss`、`index.scss` 中的页面底色和卡片基础样式 |

## 受影响文件

- `blog-vue/shoka-blog/src/assets/styles/theme-shoka.scss`
- `blog-vue/shoka-blog/src/assets/styles/common.scss`
- `blog-vue/shoka-blog/src/assets/styles/index.scss`
- `blog-vue/shoka-blog/src/views/Search/AiChat.vue`
- `blog-vue/shoka-blog/src/components/DailyRecommend/index.vue`
- `blog-vue/shoka-blog/src/views/Home/Swiper/Recommend.vue`
- `blog-vue/shoka-blog/src/views/Home/Swiper/TalkSwiper.vue`
- `blog-vue/shoka-blog/src/views/Home/LatestArticlesSection.vue`
- `blog-vue/shoka-blog/src/views/Home/CategorySections.vue`
- `blog-vue/shoka-blog/src/views/Todo/index.vue`

## 验收标准

- light 主题下背景和卡片可以明显区分。
- dark 主题下卡片从背景中能清楚浮起。
- 首页、搜索页、效率工具页的卡片风格一致。
- 没有出现过亮卡片、过重阴影或脏边。

## 测试计划

- 分别检查 light / dark 两套主题下的首页、搜索页、效率工具页。
- 检查侧边栏、推荐卡、AI 卡、Todo 卡片的背景与阴影一致性。
- 检查移动端和桌面端的层次表现。

## 风险与回滚

- 风险：背景和卡片变量改动过大可能影响整站视觉统一性。
- 风险：少量硬编码背景未覆盖时，局部仍会出现层次不一致。
- 回滚：恢复主题变量、公共样式和共用组件的改动前版本。
