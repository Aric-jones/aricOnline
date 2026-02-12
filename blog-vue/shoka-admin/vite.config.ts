import vue from "@vitejs/plugin-vue";
import path from "path";
import AutoImport from "unplugin-auto-import/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import Components from "unplugin-vue-components/vite";
import { defineConfig } from "vite";
import { prismjsPlugin } from "vite-plugin-prismjs";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 本地开发 (vite)：base 是 /，后台在 5173 端口根路径
  // 线上构建 (vite build)：base 是 /admin/，Nginx 部署在 /admin 路径下
  const base = mode === "production" ? "/admin/" : "/";

  return {
    base,
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
      }),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), "src/assets/icons")],
        symbolId: "icon-[dir]-[name]",
      }),
      prismjsPlugin({
        languages: ["java", "python", "go", "html", "json"],
        plugins: ["copy-to-clipboard"],
        theme: "okaidia",
        css: true,
      }),
    ],
    resolve: {
      alias: {
        "~": path.resolve(__dirname, "./"),
        "@": path.resolve(__dirname, "./src"),
      },
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    },
    server: {
      open: true,
      proxy: {
        "/api": {
          target: "http://localhost:8080",
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
  };
});
