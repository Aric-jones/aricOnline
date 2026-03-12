<template>
	<div class="page-header">
		<h1 class="page-title">关于</h1>
		<img
			class="page-cover"
			src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/b01e4495e1acf8e6b0b968d99dc5886b.jpg"
			alt=""
		/>
		<Bubble></Bubble>
		<waves></waves>
	</div>
	<div class="bg">
		<div class="page-container">
			<div class="avatar-box">
				<img
					class="author-avatar"
					:src="blog.blogInfoSafe.siteConfig.authorAvatar"
				/>
			</div>
			<v-md-preview
				class="md"
				:text="blog.blogInfoSafe.siteConfig.aboutMe"
			></v-md-preview>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useBlogStore } from "@/store";
const blog = useBlogStore();
</script>

<style scoped>
.author-avatar {
	width: 110px;
	height: 110px;
	border-radius: 50%;
	transition: all 0.5s;
}

.author-avatar:hover {
	transform: rotate(360deg);
}

.avatar-box {
	text-align: center;
}

@media (max-width: 767px) {
	.author-avatar {
		width: 80px;
		height: 80px;
	}

	.md {
		font-size: 0.9rem;
	}
}
</style>
