import dayjs from "dayjs";
import { fileURLToPath } from "node:url";
import { defineConfig, loadEnv } from "vite";
import {
	createViteProxy,
	setupVitePlugins
} from "./build";
export default defineConfig((configEnv) => {
	const viteEnv = loadEnv(
		configEnv.mode,
		process.cwd()
	) as Env.ImportMeta;

	const buildTime = dayjs().format("YYYY-MM-DD HH:mm:ss");

	return {
		base: viteEnv.VITE_BASE_URL,
		resolve: {
			alias: {
				"~": fileURLToPath(new URL("./", import.meta.url)),
				"@": fileURLToPath(new URL("./src", import.meta.url)),
			},
		},
		define: {
			__VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false,
			BUILD_TIME: JSON.stringify(buildTime),
		},
		plugins: setupVitePlugins(viteEnv),
		server: {
			host: "0.0.0.0",
			port: 1314,
			open: true,
			// 本地 HTTPS：将 localhost 的自签名证书放在项目根目录的 ssl/ 目录下
			// 使用 mkcert 生成：mkcert localhost 127.0.0.1
			// 生成后文件路径：ssl/localhost+1.pem 和 ssl/localhost+1-key.pem
			...(process.env.VITE_HTTPS === "true"
				? {
					https: {
						cert: "./ssl/localhost.pem",
						key: "./ssl/localhost-key.pem",
					},
				  }
				: {}),
			proxy: createViteProxy(viteEnv),
		},
		build: {
			reportCompressedSize: false,
			sourcemap: false,
			outDir: viteEnv.VITE_DIST_NAME,
			commonjsOptions: {
				ignoreTryCatch: false,
			},
		},
	};
});
