import { router } from "@/router";
import { useBlogStore, useUserStore } from "@/store";
import { getToken } from "@/utils/token";
import NProgress from "nprogress";

NProgress.configure({
  easing: "ease",
  speed: 500,
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.3,
});

const defaultTitle = "博客";

router.beforeEach((to, from, next) => {
  NProgress.start();
  const user = useUserStore();
  if (to.meta.title) {
    document.title = to.meta.title as string;
  } else {
    const siteName = useBlogStore().blogInfoSafe?.siteConfig?.siteName;
    document.title = siteName || defaultTitle;
  }
  if (getToken()) {
    if (user.id === undefined) {
      user
        .GetUserInfo()
        .then(() => {
          if (to.path === "/todo" && !user.isAdmin) {
            next("/404");
          } else {
            next();
          }
        })
        .catch(() => {
          user.LogOut().then(() => {
            window.$message?.warning("凭证失效，请重新登录");
            next();
          });
        });
    } else {
      if (to.path === "/todo" && !user.isAdmin) {
        next("/404");
      } else {
        next();
      }
    }
  } else {
    if (to.path === "/todo") {
      window.$message?.warning("请先登录");
      next("/");
    } else {
      next();
    }
  }
});
router.afterEach(() => {
  NProgress.done();
});
