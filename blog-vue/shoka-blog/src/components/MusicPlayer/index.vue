<template>
	<!-- <meting-js v-if="blog.blogInfoSafe.siteConfig.isMusic" server="netease" type="playlist"
		:id="blog.blogInfoSafe.siteConfig.musicId" fixed="true" theme="#e9546b"></meting-js> -->
	<aplayer
		v-if="blog.blogInfoSafe.siteConfig.isMusic === 1 && blog.blogInfoSafe.siteConfig.musicId"
		server="netease"
		type="playlist"
		:id="blog.blogInfoSafe.siteConfig.musicId"
		:fixed="true"
		theme="#e9546b"
	>
	</aplayer>
</template>

<script setup lang="ts">
import { useBlogStore } from "@/store";
import aplayer from "./aplayer.vue";
const blog = useBlogStore();
</script>

<style scoped></style>
