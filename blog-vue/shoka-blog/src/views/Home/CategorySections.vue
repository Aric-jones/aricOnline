<template>
	<div class="category-sections">
		<div v-if="loadError" class="load-error">
			<p>分类加载失败，请检查网络或稍后重试</p>
		</div>
		<section
			v-for="section in displayedSections"
			:key="section.id"
			class="category-section"
			v-animate="['slideUpBigIn']"
		>
			<div class="section-header">
				<h2 class="section-title">{{ section.categoryName }}</h2>
				<router-link
					:to="{
						path: '/search',
						query: {
							categoryId: section.id,
							categoryName: section.categoryName,
						},
					}"
					class="section-more"
				>
					更多
					<svg-icon icon-class="angle-right" size="0.9rem"></svg-icon>
				</router-link>
			</div>
			<div class="section-body">
				<div
					class="article-card"
					v-for="article in section.articles"
					:key="article.id"
				>
					<div class="article-cover">
						<router-link :to="`/article/${article.id}`">
							<img class="cover" v-lazy="article.articleCover" alt="" />
						</router-link>
						<span class="card-date"
							>发布于 {{ formatDate(article.createTime) }}</span
						>
					</div>
					<div class="article-info">
						<h3 class="article-title">
							<span class="top-badge" v-if="article.isTop == 1">
								<svg-icon
									icon-class="top"
									size="0.75rem"
									style="margin-right: 0.2rem"
								></svg-icon>
								置顶
							</span>
							<router-link :to="`/article/${article.id}`">
								{{ article.articleTitle }}
							</router-link>
						</h3>
						<div class="article-meta">
							<router-link
								:to="`/category/${article.category?.id}`"
								class="meta-category"
								v-if="article.category"
							>
								<svg-icon icon-class="qizhi" size="0.85rem"></svg-icon>
								{{ article.category.categoryName }}
							</router-link>
							<div class="tag-info">
								<router-link
									:to="`/tag/${tag.id}`"
									class="article-tag"
									v-for="tag in article.tagVOList || []"
									:key="tag.id"
								>
									{{ tag.tagName }}
								</router-link>
							</div>
						</div>
					</div>
				</div>
			</div>
		</section>
		<!-- 加载更多按钮 -->
		<div v-if="hasMore" class="load-more-wrapper">
			<button class="load-more-btn" @click="loadMore">
				<span v-if="loading">加载中...</span>
				<span v-else>加载更多分类</span>
				<svg-icon
					icon-class="arrow-down"
					size="0.9rem"
					style="margin-left: 0.25rem"
				></svg-icon>
			</button>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getCategoryList } from "@/api/category";
import { getCategoryArticleList } from "@/api/category";
import type { Category } from "@/api/category/types";
import type { ArticleCondition } from "@/api/article/types";
import { formatDate } from "@/utils/date";

const HOME_PAGE_SIZE = 6;
const CATEGORY_PER_PAGE = 5; // 每页显示分类数量

interface CategorySection {
	id: number;
	categoryName: string;
	articleCount: number;
	articles: ArticleCondition[];
}

const categorySections = ref<CategorySection[]>([]);
const displayedSections = ref<CategorySection[]>([]);
const currentPage = ref(1);
const loading = ref(false);
const loadError = ref(false);

onMounted(async () => {
	try {
		const { data: catRes } = await getCategoryList();
		const categories: Category[] = (catRes?.data ?? []).filter(
			(c) => c.articleCount > 0
		);
		if (categories.length === 0) {
			categorySections.value = [];
			displayedSections.value = [];
			return;
		}
		// 并行请求各分类文章，减少首屏等待时间
		const results = await Promise.allSettled(
			categories.map((cat) =>
				getCategoryArticleList({
					categoryId: cat.id,
					current: 1,
					size: HOME_PAGE_SIZE,
				})
			)
		);
		const sections: CategorySection[] = categories.map((cat, i) => {
			const res = results[i];
			// 接口返回 axios.response.data = Result，Result.data = ArticleConditionList
			const list =
				res.status === "fulfilled"
					? res.value?.data?.data?.articleConditionVOList ?? []
					: [];
			return {
				id: cat.id,
				categoryName: cat.categoryName,
				articleCount: cat.articleCount,
				articles: list,
			};
		});
		categorySections.value = sections;
		// 初始化显示第一页的分类
		updateDisplayedSections();
	} catch (_) {
		loadError.value = true;
		categorySections.value = [];
		displayedSections.value = [];
	}
});

// 计算是否还有更多分类
const hasMore = computed(() => {
	return categorySections.value.length > displayedSections.value.length;
});

// 更新显示的分类列表
const updateDisplayedSections = () => {
	const startIndex = 0;
	const endIndex = currentPage.value * CATEGORY_PER_PAGE;
	displayedSections.value = categorySections.value.slice(startIndex, endIndex);
};

// 加载更多分类
const loadMore = () => {
	if (loading.value || !hasMore.value) return;
	loading.value = true;
	// 模拟加载延迟，让用户看到加载状态
	setTimeout(() => {
		currentPage.value++;
		updateDisplayedSections();
		loading.value = false;
	}, 300);
};
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";

.category-sections {
	padding: 0 0.5rem 1rem;
}

.category-section {
	margin-bottom: 2rem;
	animation-duration: 0.5s;
	/* 不默认隐藏，保证分类下的 6 个预览能显示；v-animate 进入视口时会执行动画 */
	visibility: visible;

	&:last-child {
		margin-bottom: 0;
	}
}

.section-header {
	@include flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 1rem;
	padding: 0 0.25rem;
	border-left: 3px solid var(--color-blue);
	padding-left: 0.75rem;
}

.section-title {
	margin: 0;
	font-size: 1.25rem;
	font-weight: 600;
	color: var(--category-section-title);
}

.section-more {
	@include flex;
	align-items: center;
	gap: 0.25rem;
	font-size: 0.9rem;
	color: var(--grey-5);
	transition: color 0.2s;

	&:hover {
		color: var(--color-blue);
	}
}

/* 桌面：一行 3 个，两行共 6 个预览；平板 2 列；小屏 1 列 */
.section-body {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 1.25rem;
	min-height: 1px;
}

.article-card {
	display: flex;
	flex-direction: column;
	border-radius: 0.75rem;
	overflow: hidden;
	box-shadow: var(--card-shadow);
	transition: all 0.2s ease-in-out;
	background: var(--card-bg);

	&:hover {
		box-shadow: var(--card-shadow-hover);

		.cover {
			transform: scale(1.05);
		}
	}
}

.article-cover {
	position: relative;
	width: 100%;
	height: 11rem;
	overflow: hidden;
}

.cover {
	width: 100%;
	height: 100%;
	object-fit: cover;
	transition: transform 0.3s;
}

.card-date {
	position: absolute;
	top: 0.5rem;
	right: 0.5rem;
	font-size: 0.75rem;
	color: rgba(255, 255, 255, 0.95);
	text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
	background: rgba(0, 0, 0, 0.25);
	padding: 0.2rem 0.5rem;
	border-radius: 0.25rem;
}

.article-info {
	padding: 0.875rem 1rem;
	flex: 1;
	display: flex;
	flex-direction: column;
}

.article-title {
	font-size: 1.0625rem;
	font-weight: 600;
	line-height: 1.4;
	margin: 0 0 0.5rem;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	display: flex;
	align-items: center;
	gap: 0.4rem;
	flex-wrap: wrap;

	a {
		color: var(--article-title-color);
		flex: 1;
		min-width: 0;

		&:hover {
			color: var(--article-title-hover);
		}
	}
}

.top-badge {
	display: inline-flex;
	align-items: center;
	font-size: 0.75rem;
	color: var(--color-orange);
	font-weight: 500;
	flex-shrink: 0;
}

.article-meta {
	margin-top: auto;
	font-size: 0.8125rem;
	color: var(--article-meta-color);
	display: flex;
	align-items: center;
	justify-content: space-between;
	flex-wrap: wrap;
	gap: 0.35rem;
}

.meta-category {
	display: inline-flex;
	align-items: center;
	gap: 0.2rem;
	color: var(--category-link-color);

	&:hover {
		color: var(--category-link-hover);
	}
}

.tag-info {
	display: flex;
	flex-wrap: wrap;
	gap: 0.25rem;
}

.article-tag {
	font-size: 0.75rem;
	background: var(--article-tag-bg);
	color: var(--article-tag-color);

	&:hover {
		color: var(--article-tag-hover);
	}
}

.load-error {
	padding: 2rem 0.5rem;
	text-align: center;
	color: var(--grey-5);
	font-size: 0.95rem;
}

.load-more-wrapper {
	display: flex;
	justify-content: center;
	margin-top: 2rem;
	padding: 1rem 0;
}

.load-more-btn {
	display: inline-flex;
	align-items: center;
	padding: 0.75rem 1.5rem;
	font-size: 0.95rem;
	color: var(--grey-9);
	background: var(--card-bg);
	border: 1px solid var(--grey-4);
	border-radius: 0.5rem;
	cursor: pointer;
	transition: all 0.2s ease-in-out;
	box-shadow: var(--card-shadow);

	&:hover {
		color: var(--primary-color);
		border-color: var(--primary-color);
		box-shadow: var(--card-shadow-hover);
		transform: translateY(-2px);
	}

	&:active {
		transform: translateY(0);
	}

	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
		transform: none;
	}
}

@media (max-width: 900px) {
	.section-body {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 576px) {
	.section-body {
		grid-template-columns: 1fr;
	}
}
</style>
