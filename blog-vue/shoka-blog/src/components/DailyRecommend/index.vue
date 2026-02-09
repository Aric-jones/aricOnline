<template>
	<Teleport to="body">
		<Transition name="daily-fade">
			<div class="daily-overlay" v-if="visible" @click.self="close">
				<div class="daily-card">
					<button class="daily-close" @click="close">
						<svg-icon icon-class="close" size="1rem"></svg-icon>
					</button>
					<div class="daily-badge">每日推荐</div>
					<div class="daily-cover" v-if="article">
						<router-link :to="`/article/${article.id}`" @click="close">
							<img :src="article.articleCover" alt="article cover" />
						</router-link>
					</div>
					<div class="daily-info" v-if="article">
						<router-link
							:to="`/article/${article.id}`"
							class="daily-title"
							@click="close"
						>
							{{ article.articleTitle }}
						</router-link>
						<div class="daily-date">
							<svg-icon
								icon-class="calendar"
								size="0.8rem"
								style="margin-right: 0.3rem"
							></svg-icon>
							{{ formatDate(article.createTime) }}
						</div>
					</div>
					<div class="daily-loading" v-else>加载中...</div>
					<div class="daily-footer">
						<router-link
							v-if="article"
							:to="`/article/${article.id}`"
							class="daily-read-btn"
							@click="close"
						>
							阅读文章
						</router-link>
					</div>
				</div>
			</div>
		</Transition>
	</Teleport>
</template>

<script setup lang="ts">
import { getDailyArticle } from "@/api/article";
import { ArticleRecommend } from "@/api/article/types";
import { formatDate } from "@/utils/date";

const visible = ref(false);
const article = ref<ArticleRecommend | null>(null);

onMounted(() => {
	// 检查今天是否已经展示过
	const today = new Date().toISOString().slice(0, 10);
	const lastShown = localStorage.getItem("daily_recommend_date");
	if (lastShown === today) return;

	getDailyArticle()
		.then(({ data }) => {
			if (data.flag && data.data) {
				article.value = data.data;
				visible.value = true;
				localStorage.setItem("daily_recommend_date", today);
			}
		})
		.catch(() => {
			// 静默失败
		});
});

const close = () => {
	visible.value = false;
};
</script>

<style lang="scss" scoped>
.daily-fade-enter-active,
.daily-fade-leave-active {
	transition: opacity 0.3s ease;
}

.daily-fade-enter-from,
.daily-fade-leave-to {
	opacity: 0;
}

.daily-overlay {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 9999;
	backdrop-filter: blur(4px);
}

.daily-card {
	position: relative;
	width: 380px;
	max-width: 90vw;
	background: var(--grey-0);
	border-radius: 1rem;
	overflow: hidden;
	box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
	animation: slideUp 0.4s ease;
}

@keyframes slideUp {
	from {
		transform: translateY(30px);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

.daily-close {
	position: absolute;
	top: 0.75rem;
	right: 0.75rem;
	z-index: 10;
	background: rgba(0, 0, 0, 0.3);
	border: none;
	border-radius: 50%;
	width: 2rem;
	height: 2rem;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	color: #fff;
	transition: background 0.2s;

	&:hover {
		background: rgba(0, 0, 0, 0.5);
	}
}

.daily-badge {
	position: absolute;
	top: 0.75rem;
	left: 0.75rem;
	z-index: 10;
	background: linear-gradient(135deg, #667eea, #764ba2);
	color: #fff;
	padding: 0.25rem 0.75rem;
	border-radius: 1rem;
	font-size: 0.8rem;
	font-weight: 600;
	letter-spacing: 0.05rem;
}

.daily-cover {
	width: 100%;
	height: 200px;
	overflow: hidden;

	img {
		width: 100%;
		height: 100%;
		object-fit: cover;
		transition: transform 0.3s;

		&:hover {
			transform: scale(1.05);
		}
	}
}

.daily-info {
	padding: 1rem 1.25rem 0.5rem;
}

.daily-title {
	display: block;
	font-size: 1.1rem;
	font-weight: 600;
	color: var(--grey-8);
	line-height: 1.5;
	margin-bottom: 0.5rem;
	text-decoration: none;
	transition: color 0.2s;

	&:hover {
		color: var(--primary-color);
	}
}

.daily-date {
	display: flex;
	align-items: center;
	font-size: 0.8rem;
	color: var(--grey-5);
}

.daily-loading {
	padding: 2rem;
	text-align: center;
	color: var(--grey-5);
}

.daily-footer {
	padding: 0.75rem 1.25rem 1.25rem;
	display: flex;
	justify-content: center;
}

.daily-read-btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 0.5rem 1.5rem;
	background: linear-gradient(135deg, #667eea, #764ba2);
	color: #fff;
	border-radius: 2rem;
	font-size: 0.9rem;
	text-decoration: none;
	transition: all 0.2s;

	&:hover {
		transform: translateY(-2px);
		box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
	}
}

@media (max-width: 767px) {
	.daily-card {
		width: 320px;
	}

	.daily-cover {
		height: 160px;
	}
}
</style>
