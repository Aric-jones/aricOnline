<template>
	<div class="page-header" v-if="article">
		<div class="page-title">
			<h1 class="article-title">{{ article.articleTitle }}</h1>
			<div class="article-meta">
				<div class="first-meta">
					<span
						><svg-icon
							icon-class="calendar"
							style="margin-right: 0.15rem"
						></svg-icon>
						<span class="text">发表于 </span
						>{{ formatDate(article.createTime) }}
					</span>
					<span class="item" v-if="article.updateTime"
						><svg-icon
							icon-class="update"
							style="margin-right: 0.15rem"
						></svg-icon>
						<span class="text">更新于 </span
						>{{ formatDate(article.updateTime) }}
					</span>
					<span class="item"
						><svg-icon
							icon-class="eye"
							style="margin-right: 0.15rem"
						></svg-icon>
						<span class="text">阅读量 </span>{{ article.viewCount }}</span
					>
				</div>
				<div class="second-meta">
					<span
						><svg-icon
							icon-class="edit"
							size="0.9rem"
							style="margin-right: 0.15rem"
						></svg-icon>
						<span class="text">字数统计 </span>{{ count(wordNum) }} 字
					</span>
					<span class="item"
						><svg-icon
							icon-class="clock"
							style="margin-right: 0.15rem"
						></svg-icon>
						<span class="text">阅读时长 </span>{{ readTime }} 分钟
					</span>
					<span class="item">
						<svg-icon
							icon-class="category"
							style="margin-right: 0.15rem"
						></svg-icon
						>{{ article.category.categoryName }}
					</span>
				</div>
			</div>
		</div>
		<img class="page-cover" :src="article.articleCover" alt="" />
		<!-- 波浪 -->
		<Waves></Waves>
	</div>
	<div class="bg">
		<div class="main-container" v-if="article">
			<div class="left-container" :class="app.sideFlag ? 'w-full' : ''">
				<!-- AI 快速阅读 -->
				<div class="quick-read-card" v-if="quickReadVisible">
					<div class="quick-read-header">
						<div class="quick-read-title">
							<svg-icon
								icon-class="ai"
								size="1.2rem"
								style="margin-right: 0.4rem"
							></svg-icon>
							<span>AI 快速阅读</span>
						</div>
						<button class="quick-read-close" @click="quickReadVisible = false">
							<svg-icon icon-class="close" size="0.8rem"></svg-icon>
						</button>
					</div>
					<div class="quick-read-body">
						<div v-if="quickReadLoading" class="quick-read-loading">
							<span class="dot-loading"></span>
							AI 正在阅读文章...
						</div>
						<div v-else-if="quickReadError" class="quick-read-error">
							{{ quickReadError }}
							<button class="retry-btn" @click="fetchQuickRead">重试</button>
						</div>
						<p v-else class="quick-read-content">{{ quickReadText }}</p>
					</div>
				</div>
				<div class="article-container">
					<v-md-preview
						ref="articleRef"
						class="md"
						v-viewer
						:text="article.articleContent"
					></v-md-preview>
					<div class="article-post">
						<div class="tag-share">
							<router-link
								:to="`/tag/${tag.id}`"
								class="article-tag"
								v-for="tag in article.tagVOList"
								:key="tag.id"
							>
								<svg-icon icon-class="tag" size="0.8rem"></svg-icon>
								{{ tag.tagName }}
							</router-link>
							<Share
								class="share-info"
								:url="articleHref"
								:title="article.articleTitle"
							></Share>
						</div>
						<div class="reward">
							<button class="btn" :class="isLike(article.id)" @click="like">
								<svg-icon icon-class="like" size="0.9rem"></svg-icon> 点赞
								<span>{{ article.likeCount }}</span>
							</button>
							<n-popover
								trigger="click"
								v-if="blog.blogInfoSafe.siteConfig.isReward"
							>
								<template #trigger>
									<button class="btn reward-btn">
										<svg-icon icon-class="qr_code" size="0.9rem"></svg-icon>
										打赏
									</button>
								</template>
								<div class="reward-all">
									<span>
										<img
											class="reward-img"
											v-lazy="blog.blogInfoSafe.siteConfig.weiXinCode"
										/>
										<div class="reward-desc">微信</div>
									</span>
									<span style="margin-left: 0.3rem">
										<img
											class="reward-img"
											v-lazy="blog.blogInfoSafe.siteConfig.aliCode"
										/>
										<div class="reward-desc">支付宝</div>
									</span>
								</div>
							</n-popover>
							<p class="tea" v-if="blog.blogInfoSafe.siteConfig.isReward">
								请我喝[茶]~(￣▽￣)~*
							</p>
						</div>
						<div class="copyright">
							<ul>
								<li class="author">
									<svg-icon
										icon-class="author"
										size="0.9rem"
										style="margin-right: 0.3rem"
									></svg-icon>
									<strong>本文作者： </strong
									>{{ blog.blogInfoSafe.siteConfig.siteAuthor }}
								</li>
								<li class="link">
									<svg-icon
										icon-class="article_link"
										size="0.9rem"
										style="margin-right: 0.3rem"
									></svg-icon>
									<strong>本文链接：</strong>
									<a :href="articleHref">{{ articleHref }}</a>
								</li>
								<li class="license">
									<svg-icon
										icon-class="article_share"
										size="0.8rem"
										style="margin-right: 0.3rem"
									></svg-icon>
									<strong>版权声明： </strong>本站所有文章除特别声明外，均采用
									<a
										href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh"
										target="_blank"
										>CC BY-NC-SA 4.0</a
									>
									许可协议。转载请注明文章出处！
								</li>
							</ul>
						</div>
						<!-- 上下文 -->
						<div class="post-nav">
							<div class="item" v-if="article.lastArticle">
								<router-link
									:to="`/article/${article.lastArticle?.id}`"
									class="post-cover"
									:style="articleCover(article.lastArticle.articleCover)"
								>
									<span class="post-last-next">上一篇</span>
									<h3 class="post-title">
										{{ article.lastArticle.articleTitle }}
									</h3>
								</router-link>
							</div>
							<div class="item" v-if="article.nextArticle">
								<router-link
									:to="`/article/${article.nextArticle?.id}`"
									class="post-cover"
									:style="articleCover(article.nextArticle.articleCover)"
								>
									<span class="post-last-next">下一篇</span>
									<h3 class="post-title">
										{{ article.nextArticle.articleTitle }}
									</h3>
								</router-link>
							</div>
						</div>
						<CommentList :comment-type="commentType"></CommentList>
					</div>
				</div>
			</div>
			<div class="right-container" :class="app.sideFlag ? 'hidden' : ''">
				<div class="side-card">
					<Catalog v-if="articleLoaded" :domRef="articleRef"></Catalog>
				</div>
				<SideBarLatestArticles class="side-card"></SideBarLatestArticles>
				<SideBarPopularArticles class="side-card"></SideBarPopularArticles>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getArticle, likeArticle, quickReadArticle } from "@/api/article";
import { ArticleInfo, ArticlePagination } from "@/api/article/types";
import { CategoryVO } from "@/api/category/types";
import { useAppStore, useBlogStore, useUserStore } from "@/store";
import { formatDate } from "@/utils/date";
import { Share } from "vue3-social-share";
import "vue3-social-share/lib/index.css";
const user = useUserStore();
const app = useAppStore();
const blog = useBlogStore();
const articleRef = ref();
const route = useRoute();
const articleHref = window.location.href;
const data = reactive({
	articleLoaded: false,
	wordNum: 0,
	readTime: 0,
	commentType: 1,
	article: {
		id: 0,
		articleCover: "",
		articleTitle: "",
		articleContent: "",
		articleType: 0,
		viewCount: 0,
		likeCount: 0,
		category: {} as CategoryVO,
		tagVOList: [],
		createTime: "",
		lastArticle: {} as ArticlePagination,
		nextArticle: {} as ArticlePagination,
		updateTime: "",
	} as ArticleInfo,
});
const { articleLoaded, wordNum, readTime, commentType, article } = toRefs(data);

// AI 快速阅读
const quickReadVisible = ref(true);
const quickReadLoading = ref(false);
const quickReadText = ref("");
const quickReadError = ref("");
const fetchQuickRead = () => {
	if (!article.value.articleContent) return;
	quickReadLoading.value = true;
	quickReadError.value = "";
	quickReadArticle(article.value.articleContent)
		.then(({ data }) => {
			if (data.flag && data.data) {
				quickReadText.value = data.data;
			} else {
				quickReadError.value = data.msg || "生成失败";
			}
		})
		.catch(() => {
			quickReadError.value = "AI 服务暂时不可用，请稍后重试";
		})
		.finally(() => {
			quickReadLoading.value = false;
		});
};
const articleCover = computed(
	() => (cover: string) => "background-image:url(" + cover + ")"
);
const isLike = computed(
	() => (id: number) =>
		user.articleLikeSet.indexOf(id) != -1 ? "like-btn-active" : "like-btn"
);
const count = (value: number) => {
	if (value >= 1000) {
		return (value / 1000).toFixed(1) + "k";
	}
	return value;
};
const deleteHTMLTag = (content: string) => {
	return content
		.replace(/<\/?[^>]*>/g, "")
		.replace(/[|]*\n/, "")
		.replace(/&npsp;/gi, "");
};
const like = () => {
	if (!user.id) {
		app.setLoginFlag(true);
		return;
	}
	let id = article.value.id;
	likeArticle(id).then(({ data }) => {
		if (data.flag) {
			//判断是否点赞
			if (user.articleLikeSet.indexOf(id) != -1) {
				article.value.likeCount -= 1;
			} else {
				article.value.likeCount += 1;
			}
			user.articleLike(id);
		}
	});
};
onMounted(() => {
	getArticle(Number(route.params.id)).then(({ data }) => {
		article.value = data.data;
		const siteName = blog.blogInfoSafe?.siteConfig?.siteName;
		document.title = siteName
			? `${article.value.articleTitle} - ${siteName}`
			: article.value.articleTitle;
		wordNum.value = deleteHTMLTag(article.value.articleContent).length;
		readTime.value = Math.round(wordNum.value / 400);
		articleLoaded.value = true;
		// 自动触发 AI 快速阅读
		fetchQuickRead();
	});
});
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";

/* AI 快速阅读卡片 */
.quick-read-card {
	margin-bottom: 1rem;
	border-radius: 0.75rem;
	background: rgba(147, 112, 219, 0.12);
	backdrop-filter: blur(10px);
	border: 1px solid rgba(147, 112, 219, 0.25);
	overflow: hidden;
	box-shadow: 0 2px 12px rgba(147, 112, 219, 0.1);
	animation: fadeInDown 0.5s ease;
}

@keyframes fadeInDown {
	from {
		opacity: 0;
		transform: translateY(-10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.quick-read-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0.75rem 1rem;
	border-bottom: 1px solid rgba(147, 112, 219, 0.15);
}

.quick-read-title {
	display: flex;
	align-items: center;
	font-size: 0.95rem;
	font-weight: 600;
	color: #6a3fbf;
}

.quick-read-close {
	background: none;
	border: none;
	cursor: pointer;
	padding: 0.25rem;
	border-radius: 50%;
	color: var(--grey-5);
	transition: all 0.2s;

	&:hover {
		background: rgba(0, 0, 0, 0.06);
		color: var(--grey-7);
	}
}

.quick-read-body {
	padding: 1rem 1.25rem;
}

.quick-read-content {
	margin: 0;
	font-size: 0.9rem;
	line-height: 1.7;
	color: var(--grey-7);
}

.quick-read-loading {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	font-size: 0.9rem;
	color: #6a3fbf;
}

.dot-loading {
	display: inline-block;
	width: 1.5rem;
	height: 0.5rem;

	&::after {
		content: "...";
		animation: dotAnim 1.5s steps(3, end) infinite;
		font-size: 1.2rem;
		letter-spacing: 2px;
	}
}

@keyframes dotAnim {
	0% {
		content: ".";
	}
	33% {
		content: "..";
	}
	66% {
		content: "...";
	}
}

.quick-read-error {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	font-size: 0.9rem;
	color: #e85d5d;
}

.retry-btn {
	background: rgba(147, 112, 219, 0.15);
	border: 1px solid rgba(147, 112, 219, 0.3);
	border-radius: 0.3rem;
	padding: 0.15rem 0.5rem;
	color: #6a3fbf;
	font-size: 0.8rem;
	cursor: pointer;
	transition: all 0.2s;

	&:hover {
		background: rgba(147, 112, 219, 0.25);
	}
}

.article-container {
	border-radius: 0.5rem;
	overflow: hidden;
	box-shadow: var(--card-shadow);
}

.article-post {
	margin: 0 2rem;
	padding-bottom: 1rem;
}

.article-title {
	font-weight: 500;
	font-size: 2.5rem;
	letter-spacing: 0.125rem;
	text-align: center;
	color: var(--header-text-color);
}

.article-meta {
	@include flex;
	flex-direction: column;
	font-size: 0.875rem;

	.item {
		margin-left: 0.625rem;
	}
}

.tag-share {
	display: flex;
	align-items: center;

	.share-info {
		margin-left: auto;
	}
}

.reward {
	margin: 1.25rem auto;
	padding: 0.625rem 0;
	text-align: center;

	.btn {
		border-radius: 0.3125rem;
		color: var(--grey-0);
		cursor: pointer !important;
		padding: 0 0.9375rem;
		font: inherit;
	}

	.like-btn-active {
		background: var(--primary-color);
	}

	.like-btn {
		background: #999;
	}

	.reward-btn {
		position: relative;
		margin-left: 1rem;
		background: var(--primary-color);
	}

	.tea {
		font-size: 0.8125em;
		color: var(--grey-5);
		margin-top: 0.5rem;
	}
}

.reward-all {
	display: flex;
	align-items: center;
}

.reward-img {
	width: 130px;
	height: 130px;
	display: block;
}

.reward-desc {
	margin: -5px 0;
	color: #858585;
	text-align: center;
}

.copyright {
	font-size: 0.75em;
	padding: 1rem 2rem;
	margin-bottom: 2.5rem;
	border-radius: 0.625rem;
	background: var(--grey-2);
	color: var(--grey-6);
}

.post-nav {
	display: flex;
	margin-bottom: 2.5rem;
	border-radius: 0.625rem;
	overflow: hidden;

	.item {
		width: 50%;
	}

	.post-cover {
		display: flex;
		flex-direction: column;
		color: var(--header-text-color);
		padding: 1.25rem 2.5rem;
		background-size: cover;
		animation: blur 0.8s ease-in-out forwards;

		&:before {
			content: "";
			position: absolute;
			width: 100%;
			height: 100%;
			background: linear-gradient(135deg, #434343, #000);
			opacity: 0.5;
			transition: all 0.2s ease-in-out 0s;
			z-index: -1;
			top: 0;
			left: 0;
		}
	}

	.post-last-next {
		font-size: 0.8125rem;
	}
}

.post-cover:hover::before {
	opacity: 0.4;
}

@media (max-width: 767px) {
	.article-title {
		font-size: 1.5rem;
	}

	.article-meta .text {
		display: none;
	}

	.article-post {
		margin: 0 0.5rem;
	}

	.post-nav {
		flex-direction: column;
	}

	.post-nav .item {
		width: 100%;
	}

	.reward-img {
		width: 105px;
		height: 105px;
	}
}
</style>
