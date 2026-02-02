<!-- src/components/CyberWave.vue 赛博霓虹波动光带 -->
<template>
	<div class="cyber-wave"></div>
</template>

<script setup lang="ts">
// 纯CSS实现，全屏底部，动态波动
</script>

<style lang="scss" scoped>
.cyber-wave {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100vw;
	height: 60px; /* 光带高度，可调整 */
	z-index: 999;
	pointer-events: none;
	background: linear-gradient(90deg,
		rgba(135,206,235,0.2),
		rgba(255,105,180,0.3),
		rgba(0,255,255,0.2),
		rgba(123,104,238,0.3),
		rgba(135,206,235,0.2)
	);
	// 霓虹外发光，赛博核心
	box-shadow: 0 -5px 20px rgba(135,206,235,0.6), 0 -2px 30px rgba(255,105,180,0.4);
	border-top: 1px solid rgba(255,255,255,0.5);
	// 波动动画：上下轻微晃动+宽度微缩，模拟波浪
	animation: cyber-wave 8s ease-in-out infinite alternate;
}

// 波动核心动画
@keyframes cyber-wave {
	0% {
		transform: translateY(0) scaleX(1);
		opacity: 0.8;
		box-shadow: 0 -5px 20px rgba(135,206,235,0.6), 0 -2px 30px rgba(255,105,180,0.4);
	}
	100% {
		transform: translateY(-8px) scaleX(1.02);
		opacity: 1;
		box-shadow: 0 -8px 25px rgba(0,255,255,0.7), 0 -3px 35px rgba(123,104,238,0.5);
	}
}

// 移动端适配：缩小高度和阴影
@media (max-width: 768px) {
	.cyber-wave {
		height: 40px;
		box-shadow: 0 -3px 15px rgba(135,206,235,0.5), 0 -1px 20px rgba(255,105,180,0.3);
	}
}
</style>
