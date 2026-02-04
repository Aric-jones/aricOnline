<template>
	<div ref="brandRef" class="brand-container">
		<img
			class="page-cover"
			src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/a2a142d5efa734e270bf022d9beda5d8.jpg"
			alt="页面封面"
		/>
		<div class="brand">
			<!-- 标题 -->
			<p class="artboard">{{ blog.blogInfoSafe.siteConfig.siteName }}</p>
			<!-- 打字机 -->
			<div class="title">
				{{ obj.output }}
				<span class="easy-typed-cursor">|</span>
			</div>
		</div>
		<!-- 波浪 -->
		<Waves></Waves>
		<!-- 向下按钮 -->
		<svg-icon
			class="arrow-down"
			icon-class="down"
			size="32px"
			@click="scrollDown"
		></svg-icon>
	</div>
</template>

<script setup lang="ts">
import { useBlogStore } from "@/store";
import EasyTyper from "easy-typer-js";
const blog = useBlogStore();
const obj = reactive({
	output: "",
	isEnd: false,
	speed: 100,
	singleBack: false,
	sleep: 0,
	type: "normal",
	backSpeed: 100,
	sentencePause: false,
});
const brandRef = ref<HTMLElement>();
const scrollDown = () => {
	nextTick(() => {
		window.scrollTo({
			behavior: "smooth",
			top: brandRef.value?.clientHeight,
		});
	});
};
// 定义定时器变量，用于后续销毁
let hitokotoTimer: NodeJS.Timeout | null = null;

// 封装一言请求+打字机执行函数（核心：新增重置逻辑+兜底文案）
const fetchData = async () => {
	try {
		// 1. 重置打字机状态（清空输出、重置结束标识），避免新老文案拼接
		obj.output = "";
		obj.isEnd = false;

		// 2. 请求一言接口（改用async/await，代码更简洁）
		const res = await fetch("https://v1.hitokoto.cn");
		// 判断接口响应是否成功（非200状态抛错）
		if (!res.ok) throw new Error("接口请求失败");

		const { hitokoto } = await res.json();
		// 3. 触发打字机效果（原有逻辑不变）
		new EasyTyper(
			obj,
			hitokoto,
			() => {},
			() => {}
		);
	} catch (err) {
		// 异常处理：请求失败时显示兜底文案
		console.error("一言接口请求失败：", err);
		obj.output = "人生如逆旅，我亦是行人。"; // 兜底静态文案
	}
};

// 封装定时执行函数：初始化执行一次 + 每隔10秒执行一次
const startHitokotoTimer = () => {
	// 初始化立即执行一次，避免页面加载后空白10秒
	fetchData();
	// 设置10秒定时器（10000毫秒 = 10秒），每隔10秒调用一次fetchData
	hitokotoTimer = setInterval(fetchData, 10000);
};
onMounted(() => {
	startHitokotoTimer();
});
// 组件卸载时销毁定时器（不变）
onUnmounted(() => {
	if (hitokotoTimer) clearTimeout(hitokotoTimer);
});
</script>

<style lang="scss" scoped>
@import "@/assets/styles/mixin.scss";

.brand-container {
	@include flex;
	flex-direction: column;
	position: relative;
	width: 100%;
	height: 70vh;
	min-height: 10rem;
	color: var(--header-text-color);
}

.brand {
	@include flex;
	flex-direction: column;
	position: absolute; // 基于父容器.brand-container定位，随页面滚动
	// 核心新增：锁死位置，和背景图相对不变（按需调整top/left，推荐居中）
	top: 50%; // 距离容器顶部40%，可根据视觉需求改（如30%/50%）
	left: 50%; // 距离容器左侧50%
	transform: translate(-50%, -50%); // 左移50%+上移50%，实现水平+垂直居中
	z-index: -1; // 层级在背景图上方，不遮挡按钮/波浪

	.artboard {
		font-family: "Fredericka the Great", Mulish, -apple-system, "PingFang SC",
			"Microsoft YaHei", sans-serif;
		font-size: 3.5em;
		line-height: 1.2;
		animation: titleScale 1s;
	}

	.title {
		letter-spacing: 0.1em;
	}
}

.easy-typed-cursor {
	margin-left: 0.625rem;
	opacity: 1;
	-webkit-animation: blink 0.7s infinite;
	-moz-animation: blink 0.7s infinite;
	animation: blink 0.7s infinite;
}

.arrow-down {
	position: absolute;
	bottom: 70px;
	-webkit-animation: arrow-shake 1.5s ease-out infinite;
	animation: arrow-shake 1.5s ease-out infinite;
	cursor: pointer;
	z-index: 8;
}

.page-cover {
	// 绝对定位：基于父容器.brand-container定位，脱离文档流不影响其他元素
	position: absolute;
	top: 0;
	left: 0;
	// 铺满容器：宽度和高度100%，覆盖整个品牌容器
	width: 100%;
	height: 100%;
	// 图片适配：保持比例铺满容器，裁剪多余部分（不拉伸）
	object-fit: cover;
	// 层级控制：-2低于brand的-1，确保文字/波浪/按钮都在图片上方
	z-index: -2;
	// 可选：加轻微遮罩，让文字更清晰（根据需求调整透明度）
	filter: brightness(0.7);
}

@media (max-width: 767px) {
	.brand-container {
		padding: 3rem 0.5rem 0;
	}
}

@media (min-width: 760px) {
	.title {
		font-size: 1.5rem;
	}
}

@keyframes arrow-shake {
	0% {
		opacity: 1;
		transform: translateY(0);
	}

	30% {
		opacity: 0.5;
		transform: translateY(25px);
	}

	100% {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes blink {
	0% {
		opacity: 0;
	}

	100% {
		opacity: 1;
	}
}
</style>
