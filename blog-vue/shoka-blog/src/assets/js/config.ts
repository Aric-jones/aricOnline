// 生产环境回调地址基础（https）
const PROD_BASE = "https://ariconline.top";

// 本地开发回调地址（通过 .env.dev 中的 VITE_OAUTH_REDIRECT_BASE 配置内网穿透地址）
const DEV_BASE = import.meta.env.VITE_OAUTH_REDIRECT_BASE || "http://localhost:1314";

const BASE = import.meta.env.DEV ? DEV_BASE : PROD_BASE;

export default {
  // gitee 的 client-id
  GITEE_APP_ID: "9c4e82e111ba456c53029a26f2373d1ab3dc5795b3c042518de1833ec2127ca8",
  // gitee 回调地址
  GITEE_REDIRECT_URL: `${BASE}/oauth/login/gitee`,
  // github 的 client-id
  GITHUB_APP_ID: "Iv23liHQ5flTeS5iKcBf",
  // github 回调地址
  GITHUB_REDIRECT_URL: `${BASE}/oauth/login/github`,
  // qq 的 app-id
  QQ_APP_ID: "102885293",
  // qq 的回调地址
  QQ_REDIRECT_URL: `${BASE}/oauth/login/qq`,
};
