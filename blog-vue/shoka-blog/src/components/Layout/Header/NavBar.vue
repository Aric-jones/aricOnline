<template>
	<div class="menu">
		<div class="menu-item title">
			<router-link to="/" class="menu-btn">
				{{ blog.blogInfoSafe.siteConfig?.siteName ?? '博客' }}
			</router-link>
		</div>
		<template v-for="menu of menuList" :key="menu.name">
			<div
				v-if="!menu.children"
				class="menu-item"
				:class="{ active: route.meta.title === menu.name }"
			>
				<router-link :to="menu.path" class="menu-btn">
					<svg-icon :icon-class="menu.icon"></svg-icon> {{ menu.name }}
				</router-link>
			</div>
			<div v-else class="menu-item dropdown">
				<a class="menu-btn drop">
					<svg-icon :icon-class="menu.icon"></svg-icon> {{ menu.name }}
				</a>
				<ul class="submenu">
					<li
						class="subitem"
						v-for="submenu of menu.children"
						:key="submenu.name"
						:class="{ active: route.meta.title === submenu.name }"
					>
						<router-link class="link" :to="submenu.path">
							<svg-icon :icon-class="submenu.icon"></svg-icon>
							{{ submenu.name }}
						</router-link>
					</li>
				</ul>
			</div>
		</template>
		<div class="menu-item">
			<a v-if="!user.id" @click="app.loginFlag = true" class="menu-btn">
				<svg-icon icon-class="user"></svg-icon> 登录
			</a>
			<template v-else>
				<img class="user-avatar drop" :src="user.avatar" />
				<ul class="submenu">
					<li
						class="subitem"
						:class="{ active: route.meta.title === '个人中心' }"
					>
						<router-link to="/user" class="link">
							<svg-icon icon-class="author"></svg-icon> 个人中心
						</router-link>
					</li>
					<li class="subitem">
						<a class="link" @click="logout"
							><svg-icon icon-class="logout"></svg-icon> 退出
						</a>
					</li>
				</ul>
			</template>
		</div>
		<div class="menu-item search-item">
			<div class="search-input-container">
				<input
					ref="inputRef"
					class="search-input"
					v-model="keyword"
					placeholder="搜索..."
					@keyup.enter="handleSearch"
					@focus="showHistory = true"
					@blur="delayHideHistory"
				/>
				<svg-icon
					icon-class="search"
					class="search-icon"
					@click="handleIconClick"
				></svg-icon>
				<!-- 历史记录下拉 -->
				<ul
					class="submenu history-list"
					v-if="showHistory && historyList.length > 0"
				>
					<li
						class="subitem"
						v-for="(item, index) in historyList"
						:key="index"
						@click="handleHistoryClick(item)"
					>
						<span class="history-text">{{ item }}</span>
						<svg-icon
							icon-class="close"
							class="delete-icon"
							@click.stop="deleteHistory(index)"
						></svg-icon>
					</li>
					<li class="subitem clear-history" @click="clearHistory">
						<span>清空历史</span>
					</li>
				</ul>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useAppStore, useBlogStore, useUserStore } from "@/store";
import { useStorage } from "@vueuse/core";
const user = useUserStore();
const app = useAppStore();
const blog = useBlogStore();
const router = useRouter();
const route = useRoute();
const keyword = ref("");
const inputRef = ref<HTMLInputElement>();
const showHistory = ref(false);
const historyList = useStorage<string[]>("search_history", []);

const handleIconClick = () => {
	if (keyword.value.trim()) {
		handleSearch();
	} else {
		showHistory.value = true;
		inputRef.value?.focus();
	}
};

const handleSearch = () => {
	if (!keyword.value.trim()) return;
	// 保存历史
	if (!historyList.value.includes(keyword.value)) {
		historyList.value.unshift(keyword.value);
		if (historyList.value.length > 10) {
			historyList.value.pop();
		}
	} else {
		// 移到最前
		const index = historyList.value.indexOf(keyword.value);
		historyList.value.splice(index, 1);
		historyList.value.unshift(keyword.value);
	}
	showHistory.value = false;
	router.push({ path: "/search", query: { keyword: keyword.value } });
};

const handleHistoryClick = (item: string) => {
	keyword.value = item;
	handleSearch();
};

const deleteHistory = (index: number) => {
	historyList.value.splice(index, 1);
};

const clearHistory = () => {
	historyList.value = [];
};

const delayHideHistory = () => {
	setTimeout(() => {
		showHistory.value = false;
	}, 200);
};
const menuList = [
	{
		name: "首页",
		icon: "home",
		path: "/",
	},
	{
		name: "文章",
		icon: "article",
		children: [
			{
				name: "归档",
				icon: "archives",
				path: "/archive",
			},
			{
				name: "分类",
				icon: "category",
				path: "/category",
			},
			{
				name: "标签",
				icon: "tag",
				path: "/tag",
			},
		],
	},
	{
		name: "娱乐",
		icon: "fun",
		children: [
			{
				name: "说说",
				icon: "talk",
				path: "/talk",
			},
			{
				name: "相册",
				icon: "album",
				path: "/album",
			},
			{
				name: "图床",
				icon: "upload",
				path: "/picture",
			},
		],
	},
	{
		name: "友链",
		icon: "friend",
		path: "/friend",
	},
	{
		name: "留言板",
		icon: "message",
		path: "/message",
	},
	{
		name: "关于",
		icon: "plane",
		path: "/about",
	},
];
const logout = () => {
	if (route.path == "/user") {
		router.go(-1);
	}
	user.LogOut();
	window.$message?.success("退出成功");
};
</script>

<style lang="scss" scoped>
.user-avatar {
	display: inline-block;
	position: relative;
	top: 0.3rem;
	width: 24px;
	height: 24px;
	border-radius: 50%;
	cursor: pointer;
}

.menu {
	display: flex;
	align-items: center;
	height: 100%;
}

.menu-item {
	position: relative;
	display: inline-block;
	padding: 0 0.625rem;
	letter-spacing: 0.0625rem;
	font-size: 17px;
	text-align: center;

	&:not(.title) .menu-btn::before {
		content: "";
		position: absolute;
		width: 0;
		height: 0.1875rem;
		bottom: 0;
		border-radius: 0.125rem;
		left: 50%;
		transform: translateX(-50%);
		background-color: currentColor;
		transition: all 0.4s ease-in-out 0s;
	}

	&:hover .submenu {
		display: block;
	}
}

.menu-item.active:not(.dropdown) .menu-btn::before,
.menu-item:not(.dropdown):hover .menu-btn::before {
	width: 70%;
}

.submenu {
	display: none;
	position: absolute;
	left: 7px;
	width: max-content;
	margin-top: 0.5rem;
	padding: 0;
	background: var(--grey-9-a5);
	box-shadow: 0 0.3125rem 1.25rem -0.25rem var(--grey-9-a1);
	border-radius: 0.625rem;
	animation: slideUpIn 0.3s;

	&::before {
		position: absolute;
		top: -1.25rem;
		left: 0;
		width: 100%;
		height: 2.5rem;
		content: "";
	}
}

.search-input-container {
	display: flex;
	align-items: center;
	position: relative;
	background-color: rgba(255, 255, 255, 0.2);
	border-radius: 20px;
	padding: 4px 12px;
	transition: all 0.3s;
	margin-left: 10px;

	&:hover,
	&:focus-within {
		background-color: rgba(255, 255, 255, 0.3);
	}
}

.search-input {
	background: transparent;
	border: none;
	outline: none;
	color: currentColor;
	width: 250px;
	padding: 0 5px;
	font-size: 14px;

	&::placeholder {
		color: rgba(255, 255, 255, 0.7);
	}
}

.search-icon {
	cursor: pointer;
	font-size: 1.1rem;
	margin-left: 5px;
}

.history-list {
	display: block;
	width: 100%;
	right: 0;
	left: 0;
	top: 115%;
	border-radius: 8px;
}

.history-text {
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.delete-icon {
	font-size: 0.8rem;
	margin-left: 5px;
	float: right;
	margin-top: 5px;

	&:hover {
		color: red;
	}
}

.clear-history {
	text-align: center;
	font-size: 0.8rem;
	color: #999;
	cursor: pointer;
}

.subitem {
	display: block;
	font-size: 1rem;

	&:first-child {
		border-radius: 0.625rem 0.625rem 0 0;
	}

	&:last-child {
		border-radius: 0 0 0.625rem 0.625rem;
	}

	.link {
		display: inline-block;
		padding: 0.3rem 0.7rem;
		width: 100%;
		text-shadow: none;
	}

	&:hover .link {
		transform: translateX(0.3rem);
	}
}

.submenu .subitem.active,
.submenu .subitem:hover {
	color: var(--grey-0);
	background-image: linear-gradient(
		to right,
		var(--color-pink) 0,
		var(--color-orange) 100%
	);
	box-shadow: 0 0 0.75rem var(--color-pink-a3);
}

.sub.menu .submenu {
	background-color: var(--grey-1);
}

.drop::after {
	content: "";
	display: inline-block;
	vertical-align: middle;
	border: 0.3rem solid transparent;
	border-top-color: currentColor;
	border-bottom: 0;
}

@media (max-width: 865px) {
	.menu {
		justify-content: center;
	}

	.menu .menu-item {
		display: none;
	}

	.menu .title {
		display: block;
	}
}
</style>
