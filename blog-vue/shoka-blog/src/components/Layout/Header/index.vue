<template>
	<header class="header-wrapper" :class="fixedClass">
		<!-- 切换按钮 -->
		<Toggle></Toggle>
		<!-- 菜单 -->
		<NavBar :class="{ sub: y > 0 }"></NavBar>
		<!-- 右侧按钮 -->
		<ul class="right">
			<li class="item">
				<svg-icon
					style="cursor: pointer"
					:icon-class="themeIcon"
					@click="cycleTheme()"
				></svg-icon>
			</li>
			<!--			<li class="item">-->
			<!--				<svg-icon-->
			<!--					style="cursor: pointer"-->
			<!--					icon-class="search"-->
			<!--					@click="app.searchFlag = true"-->
			<!--				></svg-icon>-->
			<!--			</li>-->
		</ul>
	</header>
</template>

<script setup lang="ts">
import { useTheme } from "@/composables/useTheme";
import { useAppStore } from "@/store";
import { useScroll } from "@vueuse/core";

const app = useAppStore();
const { theme, cycleTheme } = useTheme();
const { y } = useScroll(window);

const themeIcon = computed(() => {
	if (theme.value === "dark") return "moon";
	if (theme.value === "gradient") return "gradient";
	return "sun";
});

const fixedClass = ref("");
watch(y, (newValue, oldValue) => {
	if (newValue > 0) {
		if (newValue < oldValue) {
			fixedClass.value = "show up";
		} else {
			fixedClass.value = "show down";
		}
	} else {
		fixedClass.value = "";
	}
});
</script>

<style lang="scss" scoped>
.header-wrapper {
	position: fixed;
	display: flex;
	flex-wrap: nowrap;
	align-items: center;
	justify-content: space-between;
	width: 100%;
	height: 3.125rem;
	padding: 0 1rem;
	text-shadow: 0 0.2rem 0.3rem rgb(0 0 0 / 50%);
	color: var(--header-text-color);
	transition: all 0.2s ease-in-out 0s;
	z-index: 9;
}

.show {
	background: var(--nav-bg);
	box-shadow: 0.1rem 0.1rem 0.2rem var(--grey-9-a1);
	text-shadow: 0 0 0.625rem var(--grey-9-a1);
	color: var(--text-color);
}

.up {
	transform: translateY(0);
}

.down {
	transform: translateY(-100%);
}

.right {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	height: 100%;

	.item {
		padding: 0.625rem 0.5rem;
	}
}

@media (max-width: 991px) {
	.header-wrapper {
		padding: 0;
	}
}
</style>
