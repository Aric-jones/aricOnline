# 文件变更规则

## 前台页面

- 先看 `blog-vue/shoka-blog/src/router/routes/index.ts`
- 再看对应 `views/` 和 `components/`
- 影响搜索、文章、效率工具时，必须更新需求文档和模块说明

## 后台管理

- 先看 `blog-vue/shoka-admin/src/store/modules/permission.ts`
- 再看对应后台 `views/` 和后端菜单接口
- 改菜单、权限、日志、监控时，必须写清影响范围

## 后端服务

- 先看对应 `controller`
- 再看 `service`、`mapper`、`entity`
- 改接口结构或数据库字段时，必须更新需求文档

## 部署配置

- 先看 `deploy/README.md`、`docker-compose.yml`、`nginx.conf`
- 改环境变量、容器、反代、证书时，必须记录回滚方式

