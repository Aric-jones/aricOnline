<template>
	<div class="category-sections">
		<div v-if="loadError" class="load-error">
			<p>分类加载失败，请检查网络或稍后重试</p>
		</div>
		<section
			v-for="section in categorySections"
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
	</div>
</template>

<script setup lang="ts">
import { getCategoryList } from "@/api/category";
import { getCategoryArticleList } from "@/api/category";
import type { Category } from "@/api/category/types";
import type { ArticleCondition } from "@/api/article/types";
import { formatDate } from "@/utils/date";

const HOME_PAGE_SIZE = 6;

interface CategorySection {
	id: number;
	categoryName: string;
	articleCount: number;
	articles: ArticleCondition[];
}

const categorySections = ref<CategorySection[]>([]);
const loadError = ref(false);

onMounted(async () => {
	try {
		const { data: catRes } = await getCategoryList();
		const categories: Category[] = (catRes?.data ?? []).filter(
			(c) => c.articleCount > 0
		);
		if (categories.length === 0) {
			categorySections.value = [];
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
	} catch (_) {
		loadError.value = true;
		categorySections.value = [];
	}
});
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
	color: var(--primary-color);
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

	a {
		color: var(--primary-color);

		&:hover {
			color: var(--color-blue);
		}
	}
}

.article-meta {
	margin-top: auto;
	font-size: 0.8125rem;
	color: var(--grey-5);
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
	color: var(--grey-7);

	&:hover {
		color: var(--color-blue);
	}
}

.tag-info {
	display: flex;
	flex-wrap: wrap;
	gap: 0.25rem;
}

.article-tag {
	font-size: 0.75rem;
	color: var(--grey-5);

	&:hover {
		color: var(--color-blue);
	}
}

.load-error {
	padding: 2rem 0.5rem;
	text-align: center;
	color: var(--grey-5);
	font-size: 0.95rem;
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
