<template>
	<!--背景轮播-->
<!--	<Images></Images>-->
	<!-- 品牌 -->
	<Brand></Brand>
	<div class="bg">
		<div class="main-container mt">
			<div class="left-container" :class="app.sideFlag ? 'test' : ''">
				<!-- 说说 -->
				<TalkSwiper></TalkSwiper>
				<!-- 推荐文章 -->
				<Recommend></Recommend>
				<!-- 最新 6 篇文章（样式同分类预览） -->
				<div class="home-sections-wrap">
					<LatestArticlesSection></LatestArticlesSection>
					<!-- 按分类展示文章 -->
					<CategorySections></CategorySections>
				</div>
			</div>
			<SideBar
				class="right-container"
				:class="app.sideFlag ? 'temp' : ''"
			></SideBar>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getBlogInfo, report } from "@/api/blogInfo";
import { useAppStore, useBlogStore } from "@/store";
import CategorySections from "./CategorySections.vue";
import LatestArticlesSection from "./LatestArticlesSection.vue";
import Brand from "./Brand/index.vue";
// import Images from "./Swiper/Images.vue";
import Recommend from "./Swiper/Recommend.vue";
import TalkSwiper from "./Swiper/TalkSwiper.vue";
const app = useAppStore();
const blog = useBlogStore();
onMounted(async () => {
	const res = await getBlogInfo();
	blog.setBlogInfo(res.data.data);
	report();
});
</script>

<style lang="scss" scoped>
.mt {
	margin-top: 1rem;
	padding-bottom: 1.75rem;
}
</style>
