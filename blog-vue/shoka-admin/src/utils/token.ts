import Cookies from "js-cookie";

// 与客户端区分，同浏览器同时登录时互不覆盖
const TokenKey: string = "AdminToken";

// 我网站的域名是www.ttkwsd.top，去前面的www，改成自己的域名
const domain: string = ".ttkwsd.top";

// token前缀
export let token_prefix = "Bearer ";

export function getToken() {
  return Cookies.get(TokenKey);
}

// 本地运行记得删除domain
export function setToken(token: string) {
  // 项目线上部署可以取消注释
  return Cookies.set(TokenKey, token);
  // return Cookies.set(TokenKey, token);
}

export function removeToken() {
  // 项目线上部署可以取消注释
  return Cookies.remove(TokenKey);
  // return Cookies.remove(TokenKey);
}
