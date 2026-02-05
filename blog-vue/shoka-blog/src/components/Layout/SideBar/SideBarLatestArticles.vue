<template>
	<div class="side-card">
		<div class="sidebar-title">
			<svg-icon icon-class="edit" size="1.1875rem"></svg-icon>
			最新文章
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
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { getArticleList } from "@/api/article";
import type { Article } from "@/api/article/types";

const list = ref<Article[]>([]);

onMounted(() => {
	getArticleList({ current: 1, size: 5 })
		.then(({ data }) => {
			list.value = data?.data?.recordList ?? [];
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
	margin-bottom: 0.5rem;

	&:last-child {
		margin-bottom: 0;
	}
}

.title {
	display: block;
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
</style>
