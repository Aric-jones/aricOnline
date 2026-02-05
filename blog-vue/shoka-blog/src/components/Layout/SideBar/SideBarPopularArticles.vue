<template>
	<div class="side-card">
		<div class="sidebar-title">
			<svg-icon icon-class="eye" size="1.1875rem"></svg-icon>
			热门文章
		</div>
		<ul class="article-list">
			<li v-for="item in list" :key="item.id" class="article-item">
				<router-link
					:to="`/article/${item.id}`"
					class="title"
					:title="item.articleTitle"
				>
					{{ item.articleTitle }}
				</router-link>
				<span class="view-count">{{ item.viewCount }} 阅读</span>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { getArticleRank } from "@/api/article";
import type { ArticleRank } from "@/api/article/types";

const list = ref<ArticleRank[]>([]);

onMounted(() => {
	getArticleRank()
		.then(({ data }) => {
			list.value = data?.data ?? [];
		})
		.catch(() => {
			list.value = [];
		});
});
</script>

<style lang="scss" scoped>
.sidebar-title {
	font-size: 1.2em;
	margin-bottom: 0.75rem;
	display: flex;
	align-items: center;
	gap: 0.4rem;
}

.article-list {
	list-style: none;
	padding: 0;
	margin: 0;
}

.article-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 0.5rem;
	margin-bottom: 0.5rem;

	&:last-child {
		margin-bottom: 0;
	}
}

.title {
	flex: 1;
	min-width: 0;
	font-size: 0.9rem;
	color: var(--grey-7);
	text-decoration: none;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	transition: color 0.2s;

	&:hover {
		color: var(--primary-color);
	}
}

.view-count {
	flex-shrink: 0;
	font-size: 0.75rem;
	color: var(--grey-5);
}
</style>
