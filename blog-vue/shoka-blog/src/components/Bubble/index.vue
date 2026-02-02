<!-- src/components/ColorBubble.vue 最上层显示 -->
<template>
	<div class="color-bubble">
		<div class="bubble b1"></div><div class="bubble b2"></div><div class="bubble b3"></div>
		<div class="bubble b4"></div><div class="bubble b5"></div><div class="bubble b6"></div>
		<div class="bubble b7"></div><div class="bubble b8"></div><div class="bubble b9"></div>
		<div class="bubble b10"></div><div class="bubble b11"></div><div class="bubble b12"></div>
		<div class="bubble b13"></div><div class="bubble b14"></div><div class="bubble b15"></div>
		<div class="bubble b16"></div><div class="bubble b17"></div><div class="bubble b18"></div>
		<div class="bubble b19"></div><div class="bubble b20"></div>
	</div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.color-bubble {
	position: absolute;
	bottom: 0;
	left: 10vw;
	/* 关键1：z-index设为999（足够大，覆盖大部分页面元素） */
	z-index: 999;
	width: 80vw;
	height: 100vh;
	/* 关键2：取消overflow: hidden（避免泡泡被容器裁剪，确保全显示） */
	/* overflow: hidden; */
	pointer-events: none; // 不遮挡鼠标点击，不影响博客操作
}

.bubble {
	position: absolute;
	border-radius: 50%;
	bottom: -50px;
	animation: bubble-up linear infinite;
	background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(255,255,255,0.2));
	border: 1px solid rgba(255,255,255,0.6);
	/* 关键3：给每个泡泡也加z-index，确保层级统一在上层 */
	z-index: 999;
}

// 以下泡泡样式不变，直接保留
.b1 { width:15px;height:15px;left:2%;animation-delay:0s;animation-duration:14s;background-color:rgba(255,105,180,0.4);}
.b2 { width:28px;height:28px;left:98%;animation-delay:1s;animation-duration:9s;background-color:rgba(135,206,235,0.4);}
.b3 { width:10px;height:10px;left:12%;animation-delay:2s;animation-duration:18s;background-color:rgba(152,251,152,0.4);}
.b4 { width:32px;height:32px;left:88%;animation-delay:3s;animation-duration:7s;background-color:rgba(255,255,105,0.4);}
.b5 { width:19px;height:19px;left:22%;animation-delay:4s;animation-duration:12s;background-color:rgba(218,112,214,0.4);}
.b6 { width:12px;height:12px;left:78%;animation-delay:5s;animation-duration:17s;background-color:rgba(100,149,237,0.4);}
.b7 { width:30px;height:30px;left:32%;animation-delay:6s;animation-duration:10s;background-color:rgba(255,165,0,0.4);}
.b8 { width:8px;height:8px;left:68%;animation-delay:7s;animation-duration:22s;background-color:rgba(240,128,128,0.4);}
.b9 { width:23px;height:23px;left:42%;animation-delay:8s;animation-duration:11s;background-color:rgba(0,255,255,0.4);}
.b10{ width:14px;height:14px;left:58%;animation-delay:9s;animation-duration:16s;background-color:rgba(123,104,238,0.4);}
.b11{ width:9px;height:9px;left:7%;animation-delay:10s;animation-duration:20s;background-color:rgba(255,192,203,0.4);}
.b12{ width:27px;height:27px;left:93%;animation-delay:11s;animation-duration:8s;background-color:rgba(32,178,170,0.4);}
.b13{ width:11px;height:11px;left:17%;animation-delay:12s;animation-duration:19s;background-color:rgba(255,215,0,0.4);}
.b14{ width:21px;height:21px;left:83%;animation-delay:13s;animation-duration:13s;background-color:rgba(173,216,230,0.4);}
.b15{ width:7px;height:7px;left:27%;animation-delay:14s;animation-duration:21s;background-color:rgba(144,238,144,0.4);}
.b16{ width:25px;height:25px;left:73%;animation-delay:15s;animation-duration:10s;background-color:rgba(255,140,0,0.4);}
.b17{ width:13px;height:13px;left:37%;animation-delay:16s;animation-duration:15s;background-color:rgba(230,230,250,0.4);}
.b18{ width:20px;height:20px;left:63%;animation-delay:17s;animation-duration:12s;background-color:rgba(0,255,127,0.4);}
.b19{ width:24px;height:24px;left:47%;animation-delay:18s;animation-duration:9s;background-color:rgba(199,21,133,0.4);}
.b20{ width:16px;height:16px;left:53%;animation-delay:19s;animation-duration:18s;background-color:rgba(70,130,180,0.4);}
.b21{ width:18px;height:18px;left:3%;animation-delay:2s;animation-duration:14s;background-color:rgba(255,105,180,0.4);}
.b22{ width:29px;height:29px;left:97%;animation-delay:5s;animation-duration:8s;background-color:rgba(135,206,235,0.4);}
.b23{ width:10px;height:10px;left:13%;animation-delay:8s;animation-duration:20s;background-color:rgba(152,251,152,0.4);}
.b24{ width:22px;height:22px;left:87%;animation-delay:11s;animation-duration:11s;background-color:rgba(255,255,105,0.4);}
.b25{ width:14px;height:14px;left:23%;animation-delay:14s;animation-duration:17s;background-color:rgba(218,112,214,0.4);}
.b26{ width:26px;height:26px;left:77%;animation-delay:17s;background-color:rgba(100,149,237,0.4);animation-duration:10s;}
.b27{ width:9px;height:9px;left:33%;animation-delay:1s;animation-duration:22s;background-color:rgba(255,165,0,0.4);}
.b28{ width:22px;height:22px;left:67%;animation-delay:4s;animation-duration:13s;background-color:rgba(240,128,128,0.4);}
.b29{ width:12px;height:12px;left:43%;animation-delay:7s;animation-duration:19s;background-color:rgba(0,255,255,0.4);}
.b30{ width:20px;height:20px;left:57%;animation-delay:10s;animation-duration:15s;background-color:rgba(123,104,238,0.4);}
.b31{ width:17px;height:17px;left:53%;animation-delay:13s;animation-duration:12s;background-color:rgba(255,192,203,0.4);}
.b32{ width:8px;height:8px;left:63%;animation-delay:16s;animation-duration:21s;background-color:rgba(32,178,170,0.4);}
.b33{ width:31px;height:31px;left:73%;animation-delay:9s;animation-duration:8s;background-color:rgba(255,215,0.4);}
.b34{ width:10px;height:10px;left:83%;animation-delay:12s;animation-duration:16s;background-color:rgba(173,216,230,0.4);}
.b35{ width:19px;height:19px;left:93%;animation-delay:15s;animation-duration:11s;background-color:rgba(144,238,144,0.4);}


@keyframes bubble-up {
	0% {
		transform: translateY(0) translateX(0);
		opacity: 0;
	}
	10% {
		opacity: 1;
	}
	90% {
		opacity: 1;
	}
	100% {
		transform: translateY(-100vh) translateX(20px);
		opacity: 0;
	}
}

// 移动端可选隐藏
// @media (max-width: 768px) {
//   .color-bubble { display: none; }
// }
</style>
