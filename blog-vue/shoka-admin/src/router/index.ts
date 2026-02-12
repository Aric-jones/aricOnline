import Layout from "@/layouts/index.vue";
import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/redirect",
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: "/redirect/:path(.*)",
        component: () => import("@/views/redirect/index.vue"),
      },
    ],
  },
  {
    path: "/login",
    name: "登录",
    component: () => import("@/views/login/index.vue"),
    meta: {
      hidden: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/error/404.vue"),
    meta: {
      hidden: true,
    },
  },
  {
    path: "",
    component: Layout,
    redirect: "/index",
    children: [
      {
        path: "/index",
        component: () => import("@/views/home/index.vue"),
        name: "Index",
        meta: { title: "首页", icon: "dashboard", affix: true },
      },
    ],
  },
];

const router = createRouter({
  // base 与 vite.config.ts 中的 base 保持一致
  // 线上部署：base 是 /admin，本地开发：base 是 /
  // import.meta.env.BASE_URL 可能带尾部斜杠，需要去掉
  history: createWebHistory(import.meta.env.BASE_URL.replace(/\/$/, '') || '/'),
  routes: constantRoutes,
});

export default router;
