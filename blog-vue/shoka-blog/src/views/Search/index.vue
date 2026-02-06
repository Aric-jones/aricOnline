<template>
	<div class="page-header">
		<h1 class="page-title">{{ pageTitle }}</h1>
		<img
			class="page-cover"
			src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/259530df534ba373663bf762e5dc3d36.jpg"
			alt=""
		/>
		<Waves></Waves>
	</div>
	<div class="bg">
		<div class="main-container mt">
			<div class="left-container">
				<div
					class="article-item"
					v-animate="['slideUpBigIn']"
					v-for="article of articleList"
					:key="article.id"
				>
					<div class="article-cover">
						<router-link :to="`/article/${article.id}`" href="">
							<img class="cover" v-lazy="article.articleCover" />
						</router-link>
					</div>
					<div class="article-info">
						<div class="article-meta">
							<span class="top" v-if="'isTop' in article && article.isTop == 1">
								<svg-icon
									icon-class="top"
									size="0.85rem"
									style="margin-right: 0.15rem"
								></svg-icon
								>置顶</span
							>
							<span class="meta-item ml-3.75">
								<svg-icon
									icon-class="calendar"
									size="0.9rem"
									style="margin-right: 0.15rem"
								></svg-icon
								>{{ formatDate(article.createTime) }}
							</span>
							<router-link
								class="meta-item ml-3.75"
								:to="{
									path: '/search',
									query: { tagId: tag.id, tagName: tag.tagName },
								}"
								v-for="tag in article.tagVOList || []"
								:key="tag.id"
							>
								<svg-icon
									icon-class="tag"
									size="0.9rem"
									style="margin-right: 0.15rem"
								></svg-icon
								>{{ tag.tagName }}
							</router-link>
						</div>
						<h3 class="article-title">
							<router-link
								:to="`/article/${article.id}`"
								v-html="article.articleTitle"
							></router-link>
						</h3>
						<!-- 概要：与以前首页文章列表一致，优先显示 articleDesc，否则显示 articleContent -->
						<div class="article-content" v-if="articleSummary(article)">
							<template v-if="article.articleDesc">{{
								article.articleDesc
							}}</template>
							<span
								v-else-if="(article as any).articleContent"
								v-html="(article as any).articleContent"
							></span>
						</div>
						<div class="article-category">
							<svg-icon
								icon-class="qizhi"
								size="0.85rem"
								style="margin-right: 0.15rem"
							></svg-icon>
							<router-link :to="categoryLink(article)" v-if="article.category">
								{{ article.category.categoryName }}
							</router-link>
						</div>
						<router-link class="article-btn" :to="`/article/${article.id}`"
							>more...</router-link
						>
					</div>
				</div>
				<div v-if="articleList.length === 0 && !loading" class="no-result">
					{{
						tagId
							? "该标签暂无文章"
							: categoryId
							? "该分类暂无文章"
							: "没有找到相关文章"
					}}
				</div>
				<div
					class="pagination-wrap"
					v-if="(categoryId || tagId) && totalPages > 1"
				>
					<Pagination
						v-model:current="listPage"
						:total="totalPages"
					></Pagination>
				</div>
			</div>
			<SideBar class="right-container"></SideBar>
		</div>
	</div>
</template>

<script setup lang="ts">
import { searchArticle } from "@/api/article";
import { getCategoryList, getCategoryArticleList } from "@/api/category";
import { getTagList, getTagArticleList } from "@/api/tag";
import type { ArticleSearch } from "@/api/article/types";
import type { ArticleCondition } from "@/api/article/types";
import SideBar from "@/components/Layout/SideBar/index.vue";
import Pagination from "@/components/Pagination/index.vue";
import { formatDate } from "@/utils/date";

const route = useRoute();

type ListItem = ArticleSearch | ArticleCondition;
const articleList = ref<ListItem[]>([]);
const loading = ref(false);

const keyword = computed(() => (route.query.keyword as string) ?? "");
const categoryId = computed(() => {
	const id = route.query.categoryId;
	if (id == null || id === "") return null;
	const n = Number(id);
	return Number.isNaN(n) ? null : n;
});
const categoryName = computed(() => (route.query.categoryName as string) ?? "");
const categoryNameFromApi = ref("");

const tagId = computed(() => {
	const id = route.query.tagId;
	if (id == null || id === "") return null;
	const n = Number(id);
	return Number.isNaN(n) ? null : n;
});
const tagName = computed(() => (route.query.tagName as string) ?? "");
const tagNameFromApi = ref("");

const pageTitle = computed(() => {
	if (tagId.value != null) {
		const name = tagName.value || tagNameFromApi.value;
		return name ? `标签：${name}` : "标签文章";
	}
	if (categoryId.value != null) {
		const name = categoryName.value || categoryNameFromApi.value;
		return name ? `分类：${name}` : "分类文章";
	}
	return "搜索结果";
});

const PAGE_SIZE = 5;
const categoryPage = ref(1);
const categoryTotal = ref(0);
const tagPage = ref(1);
const tagTotal = ref(0);

const listTotal = computed(() => {
	if (tagId.value != null) return tagTotal.value;
	if (categoryId.value != null) return categoryTotal.value;
	return 0;
});
const totalPages = computed(() => Math.ceil(listTotal.value / PAGE_SIZE) || 1);
const listPage = computed({
	get: () => (tagId.value != null ? tagPage.value : categoryPage.value),
	set: (v: number) => {
		if (tagId.value != null) tagPage.value = v;
		else categoryPage.value = v;
	},
});

function categoryLink(article: ListItem) {
	const c = (article as any).category;
	if (!c?.id) return "/search";
	return {
		path: "/search",
		query: { categoryId: c.id, categoryName: c.categoryName },
	};
}

/** 是否有概要可展示（与以前首页文章列表一致：articleDesc 或 articleContent） */
function articleSummary(article: ListItem): boolean {
	const a = article as any;
	return !!(a?.articleDesc?.trim() || a?.articleContent?.trim());
}

function loadByKeyword() {
	if (!keyword.value) {
		articleList.value = [];
		return;
	}
	loading.value = true;
	searchArticle(keyword.value)
		.then(({ data }) => {
			articleList.value = (data?.data ?? []) as ListItem[];
		})
		.finally(() => {
			loading.value = false;
		});
}

async function loadByCategory() {
	const cid = categoryId.value;
	if (cid == null) {
		articleList.value = [];
		return;
	}
	loading.value = true;
	try {
		const [listRes, catRes] = await Promise.all([
			getCategoryArticleList({
				categoryId: cid,
				current: categoryPage.value,
				size: PAGE_SIZE,
			}),
			getCategoryList(),
		]);
		const resultData = listRes?.data;
		articleList.value = (resultData?.data?.articleConditionVOList ??
			[]) as ListItem[];
		const categories = catRes?.data?.data ?? [];
		const cat = categories.find((c: { id: number }) => c.id === cid);
		categoryTotal.value = cat?.articleCount ?? 0;
		categoryNameFromApi.value = cat?.categoryName ?? "";
	} finally {
		loading.value = false;
	}
}

async function loadByTag() {
	const tid = tagId.value;
	if (tid == null) {
		articleList.value = [];
		return;
	}
	loading.value = true;
	try {
		const [listRes, tagsRes] = await Promise.all([
			getTagArticleList({
				tagId: tid,
				current: tagPage.value,
				size: PAGE_SIZE,
			}),
			getTagList(),
		]);
		const resultData = listRes?.data;
		const conditionData = resultData?.data;
		articleList.value = (conditionData?.articleConditionVOList ??
			[]) as ListItem[];
		tagNameFromApi.value = conditionData?.name ?? "";
		const tags = tagsRes?.data?.data ?? [];
		const t = tags.find((x: { id: number }) => x.id === tid);
		tagTotal.value = t?.articleCount ?? 0;
		if (!tagNameFromApi.value && t?.tagName) tagNameFromApi.value = t.tagName;
	} finally {
		loading.value = false;
	}
}

function handleLoad() {
	if (tagId.value != null) {
		loadByTag();
	} else if (categoryId.value != null) {
		loadByCategory();
	} else {
		loadByKeyword();
	}
}

watch(
	[
		() => route.query.keyword,
		() => route.query.categoryId,
		() => route.query.categoryName,
		() => route.query.tagId,
		() => route.query.tagName,
	],
	() => {
		categoryPage.value = 1;
		tagPage.value = 1;
		handleLoad();
	}
);

watch(categoryPage, () => {
	if (categoryId.value != null) loadByCategory();
});
watch(tagPage, () => {
	if (tagId.value != null) loadByTag();
});

onMounted(() => {
	handleLoad();
});
</script>

<style lang="scss" scoped>
.mt {
	margin-top: 1rem;
	padding-bottom: 1.75rem;
}

.article-item {
	display: flex;
	height: 14rem;
	margin: 1.25rem 0.5rem 0;
	border-radius: 0.5rem;
	box-shadow: var(--card-shadow);
	animation-duration: 0.5s;
	transition: all 0.2s ease-in-out 0s;
	visibility: hidden;

	&:hover {
		box-shadow: var(--card-shadow-hover);
		.cover {
			transform: scale(1.05) rotate(1deg);
		}
	}

	&:nth-child(even) {
		flex-direction: row-reverse;
		.article-cover {
			margin-right: auto;
			margin-left: 1.5rem;
			-webkit-clip-path: polygon(0 0, 100% 0, 100% 100%, 8% 100%);
			clip-path: polygon(0 0, 100% 0, 100% 100%, 8% 100%);
			border-radius: 0 0.625rem 0.625rem 0;
		}
		.article-info {
			padding: 1rem 0 3rem 1.5rem;
			.article-meta {
				justify-content: flex-start;
			}
			.article-category {
				justify-content: flex-start;
				right: auto;
				left: 0;
			}
		}
	}
}

.article-cover {
	width: 45%;
	height: 100%;
	overflow: hidden;
	margin-right: 1.5rem;
	-webkit-clip-path: polygon(0 0, 92% 0, 100% 100%, 0 100%);
	clip-path: polygon(0 0, 92% 0, 100% 100%, 0 100%);
	border-radius: 0.625rem 0 0 0.625rem;
}

.cover {
	width: 100%;
	height: 100%;
	object-fit: cover;
	transition: all 0.2s ease-in-out 0s;
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
	font-size: 0.8rem;
	color: #888;
}

.top {
	color: #ff7242;
	margin-left: 0.625rem;
}

.meta-item {
	display: flex;
	align-items: center;
}

.ml-3\.75 {
	margin-left: 0.9375rem;
}

.article-title {
	margin-top: 0.625rem;
	margin-bottom: 0.625rem;
	font-size: 1.5rem;
	font-weight: 700;
	line-height: 1.5;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
	a {
		transition: all 0.2s ease-in-out 0s;
		&:hover {
			color: #49b1f5;
		}
	}
}

.article-content {
	font-size: 0.9rem;
	line-height: 2;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
}

.article-category {
	position: absolute;
	display: flex;
	align-items: center;
	bottom: 0.5rem;
	font-size: 0.85rem;
	color: #858585;
	transition: all 0.2s ease-in-out 0s;
	&:hover {
		color: #49b1f5;
	}
}

.article-btn {
	position: absolute;
	bottom: 0.5rem;
	right: 0;
	padding: 0 0.8rem;
	border-radius: 1rem;
	color: #fff;
	background-image: linear-gradient(to right, #ed6ea0 0, #ec8c69 100%);
	z-index: 1;
	&:hover {
		transform: translateZ(2.5rem);
	}
}

.no-result {
	text-align: center;
	margin-top: 2rem;
	font-size: 1.2rem;
	color: #666;
}

.pagination-wrap {
	margin-top: 2rem;
	display: flex;
	justify-content: center;
}
</style>
