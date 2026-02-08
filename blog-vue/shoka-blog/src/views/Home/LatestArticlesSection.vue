<template>
	<section class="latest-section category-section" v-animate="['slideUpBigIn']">
		<div class="section-header">
			<h2 class="section-title">最新文章</h2>
			<router-link to="/archive" class="section-more">
				更多
				<svg-icon icon-class="angle-right" size="0.9rem"></svg-icon>
			</router-link>
		</div>
		<div class="section-body">
			<div
				class="article-card"
				v-for="article in articleList"
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
							:to="`/search?categoryId=${article.category?.id}&categoryName=${article.category?.categoryName}`"
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
</template>

<script setup lang="ts">
import { getArticleList } from "@/api/article";
import type { Article } from "@/api/article/types";
import { formatDate } from "@/utils/date";

const articleList = ref<Article[]>([]);

onMounted(() => {
	getArticleList({ current: 1, size: 6 })
		.then(({ data }) => {
			articleList.value = data?.data?.recordList ?? [];
		})
		.catch(() => {
			articleList.value = [];
		});
});
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";

.latest-section {
	margin-bottom: 2rem;
	padding: 0 0.5rem;
	animation-duration: 0.5s;
	visibility: visible;
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
