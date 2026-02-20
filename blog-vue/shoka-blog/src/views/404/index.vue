<template>
	<div class="page-header">
		<h1 class="page-title">页面不存在</h1>
		<img
			class="page-cover"
			src="https://ik.imagekit.io/nicexl/Wallpaper/ba41a32b219e4b40ad055bbb52935896_Y0819msuI.jpg"
			alt=""
		/>
		<Bubble />
		<Waves></Waves>
	</div>
	<div class="bg">
		<div class="page-container">
			<div class="notfound">找不到页面啊，你是不是找错地方了？</div>
			<div class="notfound-actions">
				<router-link to="/" class="back-home">返回首页</router-link>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts"></script>

<style scoped>
.notfound {
	width: 18.75rem;
	max-width: 100%;
	height: 22.625rem;
	background: url("https://ican1999.github.io/images/404.png") no-repeat bottom;
	background-size: contain;
	text-align: center;
	margin: 6.25rem auto;
}

@media (max-width: 767px) {
	.notfound {
		margin: 3rem auto;
		height: 18rem;
	}
}

.notfound-actions {
	text-align: center;
	margin-top: -2rem;
}

.back-home {
	display: inline-block;
	padding: 0.5rem 1.25rem;
	border-radius: 0.5rem;
	color: var(--primary-color);
	border: 1px solid var(--primary-color);
	transition: all 0.2s;
}

.back-home:hover {
	background: var(--primary-color);
	color: #fff;
}
</style>
