<template>
	<div class="page-header">
		<h1 class="page-title">分类</h1>
		<img
			class="page-cover"
			src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/259530df534ba373663bf762e5dc3d36.jpg"
			alt=""
		/>
		<Bubble />
		<Waves></Waves>
	</div>
	<div class="bg">
		<div class="page-container list-container">
			<!-- 与以前文章列表一样的横向大卡片样式 -->
			<div
				class="article-item"
				v-animate="['slideUpBigIn']"
				v-for="article of articleList"
				:key="article.id"
			>
				<div class="article-cover">
					<router-link :to="`/article/${article.id}`">
						<img class="cover" v-lazy="article.articleCover" alt="" />
					</router-link>
				</div>
				<div class="article-info">
					<div class="article-meta">
						<span class="meta-item">
							<svg-icon
								icon-class="calendar"
								size="0.9rem"
								style="margin-right: 0.15rem"
							></svg-icon>
							{{ formatDate(article.createTime) }}
						</span>
						<router-link
							class="meta-item"
							:to="`/tag/${tag.id}`"
							v-for="tag in article.tagVOList || []"
							:key="tag.id"
						>
							<svg-icon
								icon-class="tag"
								size="0.9rem"
								style="margin-right: 0.15rem"
							></svg-icon>
							{{ tag.tagName }}
						</router-link>
					</div>
					<h3 class="article-title">
						<router-link :to="`/article/${article.id}`">
							{{ article.articleTitle }}
						</router-link>
					</h3>
					<div class="article-category">
						<svg-icon
							icon-class="qizhi"
							size="0.85rem"
							style="margin-right: 0.15rem"
						></svg-icon>
						<router-link
							:to="`/category/${article.category?.id}`"
							v-if="article.category"
						>
							{{ article.category.categoryName }}
						</router-link>
					</div>
					<router-link class="article-btn" :to="`/article/${article.id}`"
						>more...</router-link
					>
				</div>
			</div>
			<div class="pagination-wrap" v-if="totalPages > 1">
				<Pagination
					v-model:current="queryParams.current"
					:total="totalPages"
				></Pagination>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ArticleCondition, ArticleQuery } from "@/api/article/types";
import { getCategoryList, getCategoryArticleList } from "@/api/category";
import { formatDate } from "@/utils/date";
import Pagination from "@/components/Pagination/index.vue";

const route = useRoute();
const PAGE_SIZE = 5;

function getCategoryIdFromRoute(): number | null {
	const id = route.params?.categoryId;
	if (id == null || id === "") return null;
	const n = Number(id);
	return Number.isNaN(n) ? null : n;
}

const data = reactive({
	queryParams: {
		current: 1,
		size: PAGE_SIZE,
		categoryId: 0,
	} as ArticleQuery,
	name: "",
	articleCount: 0,
	articleList: [] as ArticleCondition[],
});
const { queryParams, name, articleCount, articleList } = toRefs(data);

const totalPages = computed(
	() => Math.ceil(articleCount.value / PAGE_SIZE) || 1
);

function loadList() {
	const cid = getCategoryIdFromRoute();
	if (cid == null) return;
	queryParams.value.categoryId = cid;
	getCategoryArticleList(queryParams.value)
		.then(({ data }) => {
			articleList.value = data?.data?.articleConditionVOList ?? [];
			name.value = data?.data?.name ?? "";
		})
		.catch(() => {
			articleList.value = [];
		});
}

watch(
	() => queryParams.value.current,
	() => {
		if (getCategoryIdFromRoute() != null) loadList();
	}
);

watch(
	() => route.params.categoryId,
	async (categoryId) => {
		const id =
			categoryId != null && categoryId !== "" ? Number(categoryId) : NaN;
		if (Number.isNaN(id)) return;
		queryParams.value.categoryId = id;
		queryParams.value.current = 1;
		const { data: catRes } = await getCategoryList();
		const cat = (catRes?.data || []).find((c: { id: number }) => c.id === id);
		articleCount.value = cat?.articleCount ?? 0;
		loadList();
	}
);

onMounted(async () => {
	const categoryId = getCategoryIdFromRoute();
	if (categoryId == null) return;
	queryParams.value.categoryId = categoryId;
	const { data: catRes } = await getCategoryList();
	const cat = (catRes?.data || []).find(
		(c: { id: number }) => c.id === categoryId
	);
	articleCount.value = cat?.articleCount ?? 0;
	loadList();
});
</script>

<style lang="scss" scoped>
.list-container {
	max-width: 56rem;
	margin: 0 auto;
	padding: 0 0.5rem;
}

.article-item {
	display: flex;
	height: 14rem;
	margin: 1.25rem 0.5rem 0;
	border-radius: 0.5rem;
	box-shadow: 0 0.625rem 1.875rem -0.9375rem var(--box-bg-shadow);
	animation-duration: 0.5s;
	transition: all 0.2s ease-in-out 0s;
	visibility: hidden;

	&:hover {
		box-shadow: 0 0 1.5rem var(--box-bg-shadow);
		.cover {
			transform: scale(1.05) rotate(1deg);
		}
	}

	&:nth-child(even) {
		flex-direction: row-reverse;
		.article-cover {
			margin-right: auto;
			margin-left: 1.5rem;
			clip-path: polygon(0 0, 100% 0, 100% 100%, 8% 100%);
			border-radius: 0 0.625rem 0.625rem 0;
		}
		.article-info {
			padding: 1rem 0 3rem 1.5rem;
			.article-meta {
				justify-content: flex-start;
			}
		}
		.article-btn {
			left: 0;
			right: auto;
			border-radius: 0 1rem;
			background-image: linear-gradient(
				to right,
				var(--color-orange) 0,
				var(--color-pink) 100%
			);
			&:hover {
				transform: translateZ(2rem);
			}
		}
		.article-category {
			right: 0.5rem;
			justify-content: flex-start;
		}
	}
}

.article-cover {
	width: 50%;
	margin-right: 1.5rem;
	clip-path: polygon(0 0, 92% 0, 100% 100%, 0 100%);
	border-radius: 0.625rem 0 0 0.625rem;
	overflow: hidden;
	.cover {
		width: 100%;
		height: 100%;
		object-fit: cover;
		transition: all 0.2s ease-in-out 0s;
	}
}

.article-info {
	position: relative;
	width: 50%;
	padding: 1rem 1.5rem 3rem 0;
	perspective: 62.5rem;
}

.article-meta {
	display: flex;
	justify-content: flex-end;
	font-size: 0.8125rem;
	color: var(--grey-5);
	flex-wrap: wrap;
	gap: 0 0.625rem;
}

.meta-item {
	display: flex;
	align-items: center;
}

.article-title {
	text-overflow: ellipsis;
	white-space: nowrap;
	margin: 0.625rem 0;
	color: var(--primary-color);
	overflow: hidden;
	a:hover {
		color: var(--color-blue);
	}
}

.article-category {
	position: absolute;
	display: flex;
	align-items: center;
	bottom: 0.5rem;
	font-size: 0.8125em;
	color: var(--grey-5);
	a:hover {
		color: var(--color-blue);
	}
}

.article-btn {
	position: absolute;
	bottom: 0;
	right: 0;
	padding: 0.3rem 1rem;
	border-radius: 1rem 0;
	color: var(--grey-0);
	background-image: linear-gradient(
		to right,
		var(--color-pink) 0,
		var(--color-orange) 100%
	);
	&:hover {
		transform: translateZ(2rem);
	}
}

.pagination-wrap {
	margin-top: 2rem;
	display: flex;
	justify-content: center;
}

@media (max-width: 767px) {
	.article-item {
		flex-direction: column;
		height: fit-content;
		.article-cover {
			width: 100%;
			height: 14rem;
			margin: auto;
			clip-path: polygon(0 0, 100% 0, 100% 92%, 0 100%);
			border-radius: 0.625rem 0.625rem 0 0;
		}
		.article-info {
			width: 100%;
			min-height: 10rem;
			padding: 0 1rem 3rem;
		}
		&:nth-child(even) {
			flex-direction: column;
			.article-cover {
				width: 100%;
				margin: auto;
				clip-path: polygon(0 0, 100% 0, 100% 100%, 0 92%);
				border-radius: 0.625rem 0.625rem 0 0;
			}
			.article-info {
				padding: 0 1rem 3rem;
			}
		}
	}
}
</style>
