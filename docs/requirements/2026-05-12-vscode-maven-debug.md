# 2026-05-12 VS Code Maven 调试配置

## 背景

本仓库后端为 Spring Boot 单体服务，开发调试时需要先下载 Maven 依赖，再编译并启动主类。当前缺少可直接复用的 VS Code 调试配置，且需要固定使用用户本地 Maven 配置与本地仓库路径。

## 目标

- 提供可直接在 VS Code 中使用的后端调试配置。
- 调试前先通过指定 Maven 配置文件和本地仓库下载/解析依赖。
- 让后端以开发环境参数启动，便于本地联调。

## 非目标

- 不修改业务代码。
- 不调整接口、数据库结构、权限逻辑或部署拓扑。
- 不替换项目现有的 Docker 部署方案。

## 功能说明

- 新增 VS Code `launch.json`，用于启动 `com.ican.BlogApplication`。
- 新增 VS Code `tasks.json`，用于在调试前执行 Maven 编译。
- 调试任务固定使用以下 Maven 路径：
  - 配置目录：`D:\app\tool\maven\apache-maven-3.9.14\conf`
  - 本地仓库：`D:\app\tool\maven\mvn_repo`
- 调试环境默认从 `deploy/.env` 读取环境变量，并通过 JVM 参数覆盖本地调试所需的主机和端口。

## 影响范围

| 类型 | 说明 |
| --- | --- |
| 开发工具 | 新增 VS Code 调试与构建任务 |
| 配置项 | Maven 配置文件、本地仓库路径、后端调试环境变量 |
| 运行方式 | 本地调试时先编译后启动后端主类 |

## 受影响文件

- `.vscode/launch.json`
- `.vscode/tasks.json`
- `docs/changelog.md`

## 验收标准

- VS Code 可以直接选择后端调试配置。
- 调试前会执行指定 Maven 配置和本地仓库的编译任务。
- 后端能够按开发环境参数启动主类。
- 不引入业务代码改动。

## 测试计划

1. 执行 Maven 编译任务，确认依赖可从本地仓库解析。
2. 在 VS Code 中启动调试配置，确认能进入 `BlogApplication`。
3. 确认运行时环境变量已覆盖为本地调试值。

## 风险与回滚

- 风险：
  - 本地 MySQL/Redis 未启动会导致应用启动失败。
  - 本地端口 4000/5000/8080 被占用会导致调试失败。
  - 用户本地 Maven `settings.xml` 不可用会导致依赖解析失败。
- 回滚：
  - 删除 `.vscode/launch.json` 和 `.vscode/tasks.json`。
  - 移除本需求文档与对应的变更日志记录。
