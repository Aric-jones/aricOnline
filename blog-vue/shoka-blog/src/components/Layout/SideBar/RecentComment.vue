<template>
	<div class="side-card">
		<div class="comment-title">
			<svg-icon icon-class="comment" size="1.1875rem"></svg-icon>
			最新评论
		</div>
		<!-- 只显示约 5 条高度，内部 10 条持续向上滚动 -->
		<div class="comment-scroll-wrap">
			<div
				class="comment-scroll-inner"
				:class="{ scroll: scrollList.length > 0 }"
			>
				<div
					class="comment-item"
					v-for="(comment, index) in scrollList"
					:key="`${comment.id}-${index}`"
				>
					<img class="user-avatar" :src="comment.avatar" alt="" />
					<div class="comment-content">
						<div class="info">
							<span class="comment-name">{{ comment.nickname }}</span>
							<div>{{ formatDate(comment.createTime) }}</div>
						</div>
						<span class="content" v-html="comment.commentContent"></span>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { getRecentComment } from "@/api/comment";
import { RecentComment } from "@/api/comment/types";
import { formatDate } from "@/utils/date";

const commentList = ref<RecentComment[]>([]);

/** 列表复制一份拼在后面，用于无缝向上滚动循环 */
const scrollList = computed(() => {
	const list = commentList.value;
	if (list.length === 0) return [];
	return [...list, ...list];
});

onMounted(() => {
	getRecentComment().then(({ data }) => {
		commentList.value = data?.data ?? [];
	});
});
</script>

<style lang="scss" scoped>
.comment-title {
	font-size: 1.2em;
}

.comment-scroll-wrap {
	overflow: hidden;
	height: 22rem; /* 约 5 条评论的高度 */
}

.comment-scroll-inner {
	&.scroll {
		animation: commentScroll 25s linear infinite;
	}
}

@keyframes commentScroll {
	0% {
		transform: translateY(0);
	}
	100% {
		transform: translateY(-50%);
	}
}

.comment-name {
	overflow: hidden;
	text-overflow: ellipsis;
	display: inherit;
}

.comment-item {
	display: flex;
	align-items: center;
	padding: 0.375rem 0;
}

.user-avatar {
	width: 4.2rem;
	height: 4.2rem;
	border-radius: 0.75rem;
}

.comment-content {
	width: calc(100% - 4.2rem);
	padding-left: 0.625rem;

	.info {
		font-size: 5%;
		line-height: 1rem;
		color: var(--grey-6);
	}

	.content {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		font-size: 95%;
		line-height: 1.5;
		overflow: hidden;
	}
}
</style>
