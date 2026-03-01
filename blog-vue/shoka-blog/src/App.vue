<template>
	<Provider>
		<div class="app-wrapper" :class="{ 'has-custom-bg': shouldShowCustomBg }">
			<Header></Header>
			<main class="main-wrapper">
				<router-view v-slot="{ Component, route }">
					<keep-alive>
						<component :is="Component" :key="route.path" />
					</keep-alive>
				</router-view>
			</main>
			<Footer></Footer>
			<Tool></Tool>
			<Search></Search>
			<Login></Login>
			<Register></Register>
			<Forget></Forget>
			<Email></Email>
			<Drawer></Drawer>
			<MusicPlayer></MusicPlayer>
			<ChatRoom></ChatRoom>
		</div>
	</Provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const shouldShowCustomBg = computed(() => {
	const path = route.path;
	// 排除首页和留言板
	return path !== '/' && path !== '/message';
});

onMounted(() => {
	console.log(
		"%c Hello World %c By 牧羊人 %c",
		"background:#e9546b ; padding: 1px; border-radius: 3px 0 0 3px;  color: #fff; padding:5px 0;",
		"background:#ec8c69 ; padding: 1px; border-radius: 0 3px 3px 0;  color: #000; padding:5px 0;",
		"background:transparent"
	);
})
</script>

<style scoped>
.app-wrapper {
	position: relative;
	min-height: 100vh;
}

.app-wrapper.has-custom-bg {
	background-image: url('https://big-event0611.oss-cn-beijing.aliyuncs.com/article/b01e4495e1acf8e6b0b968d99dc5886b.jpg');
	background-size: cover;
	background-position: center;
	background-repeat: no-repeat;
	background-attachment: fixed;
}

.main-wrapper {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 0 0 8rem;
}
</style>
