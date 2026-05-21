# Markdown 编码统一

## 背景

仓库中的第一方 markdown 文档内容本身是中文，但部分 Windows 工具和编辑器在未识别编码时会显示为乱码。用户已经反馈根目录 `README.md` 存在这个问题。

## 目标

- 将第一方 markdown 文档统一为可被常见 Windows 工具稳定识别的 UTF-8 编码。
- 保持文档内容不变，只处理编码表现问题。

## 非目标

- 不重写文案。
- 不调整产品功能、模块边界、路由、接口或部署流程。
- 不处理 `node_modules` 下的第三方文档。

## 影响范围

| 范围 | 说明 |
| --- | --- |
| 根目录说明 | `README.md` |
| 部署说明 | `deploy/README.md` |
| 项目文档 | `docs/README.md`、`docs/changelog.md`、`docs/modules.md`、`docs/project-overview.md`、`docs/quick-start-for-dev.md` |
| 需求文档 | `docs/requirements/*.md` |
| 规范文档 | `docs/standards/*.md` |

## 受影响文件

- `README.md`
- `deploy/README.md`
- `docs/README.md`
- `docs/changelog.md`
- `docs/modules.md`
- `docs/project-overview.md`
- `docs/quick-start-for-dev.md`
- `docs/requirements/2026-05-10-search-page.md`
- `docs/requirements/2026-05-10-theme-contrast-optimization.md`
- `docs/requirements/2026-05-12-home-article-card-preview.md`
- `docs/requirements/2026-05-12-vscode-maven-debug.md`
- `docs/requirements/TEMPLATE.md`
- `docs/standards/development-process.md`
- `docs/standards/file-change-rules.md`

## 验收标准

- 上述文件在常见编辑器中可直接显示中文内容。
- 文件内容未发生业务性改写。
- 仓库内第一方 markdown 的编码表现保持一致。

## 测试计划

1. 检查目标文件是否已统一为 UTF-8 编码。
2. 抽查根目录 `README.md` 和 `docs/README.md` 的显示效果。
3. 确认 `git diff` 中没有非编码相关的文案变更。

## 风险与回滚

- 风险：少数旧工具可能对 UTF-8 BOM 支持不一致。
- 回滚：移除 BOM，并恢复变更前的文件字节内容。
