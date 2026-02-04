import { BlogInfo, SiteConfig } from "@/api/blogInfo/types";
/**
 * 博客
 */
interface BlogState {
  /**
   * 博客信息
   */
  blogInfo: BlogInfo;
}

export const useBlogStore = defineStore("useBlogStore", {
	state: (): BlogState => ({
		blogInfo: {
			siteConfig: {} as SiteConfig,
		} as BlogInfo,
	}),
	actions: {
		setBlogInfo(blogInfo: BlogInfo | null) {
			if (blogInfo != null) {
				this.blogInfo = blogInfo;
			}
		},
	},
	getters: {
		/** 保证不为 null，避免 persist 还原后或首屏未请求前报错 */
		blogInfoSafe(state): BlogInfo {
			const info = state.blogInfo;
			if (info == null || info.siteConfig == null) {
				return { siteConfig: {} as SiteConfig } as BlogInfo;
			}
			return info;
		},
	},
	persist: {
		key: "blog",
		storage: sessionStorage,
	},
});
