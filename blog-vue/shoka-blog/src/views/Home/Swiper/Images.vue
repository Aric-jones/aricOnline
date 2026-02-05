<template>
	<div class="imgs">
		<!-- 图片轮播：接口有数据时展示 -->
		<ul v-if="useImageMode && carouselList.length">
			<li
				class="item item-img"
				v-for="carousel of carouselList"
				:key="carousel.id"
				:style="{ 'background-image': 'url(' + carousel.imgUrl + ')' }"
			></li>
		</ul>
		<!-- RGB 渐变：无图片或 useGradient 时展示，多层渐变错峰淡入淡出 -->
		<ul v-else class="gradient-list">
			<li class="item item-gradient g1"></li>
			<li class="item item-gradient g2"></li>
			<li class="item item-gradient g3"></li>
			<li class="item item-gradient g4"></li>
			<li class="item item-gradient g5"></li>
			<li class="item item-gradient g6"></li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { getCarouselList } from "@/api/carousel";
import { Carousel } from "@/api/carousel/types";

/** true=用接口轮播图，false=用 RGB 渐变（无图或强制渐变时） */
const useGradient = ref(true);
const carouselList = ref<Carousel[]>([]);

onMounted(() => {
	getCarouselList()
		.then(({ data }) => {
			carouselList.value = data?.data ?? [];
		})
		.catch(() => {
			carouselList.value = [];
		});
});

const useImageMode = computed(
	() => !useGradient.value && carouselList.value.length > 0
);
</script>

<style lang="scss" scoped>
.imgs {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 70vh;
	z-index: -9;
	background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
	overflow: hidden;

	ul,
	.gradient-list,
	.item {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
	}

	ul,
	.gradient-list {
		list-style: none;
		margin: 0;
		padding: 0;
	}

	.item-img {
		background: no-repeat 50% 50% / cover;
		opacity: 0;
		animation: imageAnimation 36s linear infinite 0s;
		backface-visibility: hidden;
		transform-style: preserve-3d;

		&:nth-child(2) {
			animation-delay: 6s;
		}
		&:nth-child(3) {
			animation-delay: 12s;
		}
		&:nth-child(4) {
			animation-delay: 18s;
		}
		&:nth-child(5) {
			animation-delay: 24s;
		}
		&:nth-child(6) {
			animation-delay: 30s;
		}
	}

	.item-gradient {
		background: no-repeat 50% 50% / cover;
		opacity: 0;
		animation: gradientFade 24s ease-in-out infinite;
		pointer-events: none;
	}

	.g1 {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	}
	.g2 {
		background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	}
	.g3 {
		background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	}
	.g4 {
		background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
	}
	.g5 {
		background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
	}
	.g6 {
		background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
	}

	.g1 {
		animation-delay: 0s;
	}
	.g2 {
		animation-delay: 4s;
	}
	.g3 {
		animation-delay: 8s;
	}
	.g4 {
		animation-delay: 12s;
	}
	.g5 {
		animation-delay: 16s;
	}
	.g6 {
		animation-delay: 20s;
	}

	&::before {
		content: "";
		display: block;
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.2);
		pointer-events: none;
	}
}

@keyframes imageAnimation {
	0% {
		opacity: 0;
		animation-timing-function: ease-in;
	}
	2% {
		opacity: 1;
	}
	8% {
		opacity: 1;
		transform: scale(1.05);
		animation-timing-function: ease-out;
	}
	17% {
		opacity: 1;
		transform: scale(1.1);
	}
	25% {
		opacity: 0;
		transform: scale(1.1);
	}
	100% {
		opacity: 0;
	}
}

@keyframes gradientFade {
	0% {
		opacity: 0;
	}
	8% {
		opacity: 1;
	}
	25% {
		opacity: 1;
	}
	33% {
		opacity: 0;
	}
	100% {
		opacity: 0;
	}
}
</style>
