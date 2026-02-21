import { createApp } from "vue";
import App from "./App.vue";
import { getBlogInfo } from "./api/blogInfo";
import { setupDirectives } from "./directives";
import { setupAssets, setupLazy, setupMasonry, setupMdEditor, setupMdPreview, setupViewer } from "./plugins";
import { setupRouter } from "./router";
import { setupStore, useBlogStore } from "./store";

async function setupApp() {
	setupAssets();

	const app = createApp(App);
	setupStore(app);

	// 首屏前拉取博客信息，避免 Header/NavBar 等读取 siteConfig 报错
	try {
		const res = await getBlogInfo();
		if (res?.data?.data) {
			useBlogStore().setBlogInfo(res.data.data);
		}
	} catch (_) {
		// 接口失败时使用 store 默认值，由 blogInfoSafe 兜底
	}

	setupDirectives(app);
	setupLazy(app);
	setupMdPreview(app);
	setupMdEditor(app);
	setupMasonry(app);
	setupViewer(app);
	await setupRouter(app);
	app.mount("#app");
}

setupApp();
