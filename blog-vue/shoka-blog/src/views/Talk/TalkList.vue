<template>
  <div class="page-header">
    <h1 class="page-title">说说</h1>
    <img class="page-cover" src="https://ik.imagekit.io/nicexl/Wallpaper/ba41a32b219e4b40ad055bbb52935896_Y0819msuI.jpg"
      alt="">
		<Bubble/>
    <Waves></Waves>
  </div>
  <div class="bg">
    <div class="page-container">
      <router-link :to="`/talk/${talk.id}`" class="talk-item" v-animate="['slideUpBigIn']" v-for="talk in talkList"
        :key="talk.id">
        <div class="talk-meta">
          <!-- 用户头像 -->
          <img class="user-avatar" :src="talk.avatar">
          <div class="talk-info">
            <span class="talk-user-name">{{ talk.nickname }}<svg-icon icon-class="badge"
                style="margin-left: 0.4rem;"></svg-icon></span>
            <span class="talk-time">{{ formatDateTime(talk.createTime) }}</span>
          </div>
        </div>
        <!-- 说说内容 -->
        <div class="talk-content" v-html="talk.talkContent">
        </div>
        <!-- 说说图片 -->
        <div class="talk-image" v-viewer>
          <img @click.prevent class="image" v-for="(img, index) in talk.imgList" :key="index" v-lazy="img" />
        </div>
        <!-- 说说信息 -->
        <div class="info" style="margin-top: 0.5rem;">
          <!-- 点赞量 -->
          <span class="talk-like info">
            <svg-icon icon-class="like" size="0.8rem" style="margin-right: 5px;"></svg-icon>{{
              talk.likeCount
            }}
          </span>
          <!-- 评论量 -->
          <span class="talk-comment info">
            <svg-icon icon-class="comment" size="0.9rem" style="margin-right: 5px;"></svg-icon>{{
              talk.commentCount
            }}
          </span>
        </div>
      </router-link>
      <div class="loading-warp" v-if="talkList && count > talkList.length">
        <n-button class="btn" color="#e9546b" @click="getList">
          加载更多...
        </n-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getTalkList } from "@/api/talk";
import { Talk } from "@/api/talk/types";
import { PageQuery } from "@/model";
import { formatDateTime } from "@/utils/date";
const data = reactive({
  count: 0,
  queryParams: {
    current: 1,
    size: 5
  } as PageQuery,
  talkList: [] as Talk[],
});
const {
  count,
  queryParams,
  talkList,
} = toRefs(data);
const getList = () => {
  getTalkList(queryParams.value).then(({ data }) => {
    if (queryParams.value.current == 1) {
      talkList.value = data.data.recordList;
    } else {
      talkList.value.push(...data.data.recordList);
    }
    queryParams.value.current++;
    count.value = data.data.count;
  });
};
onMounted(() => {
  getList();
})
</script>

<style lang="scss" scoped>
// 原有样式保留，只新增/修改图片相关、补充必要样式
.talk-item {
	display: flex;
	flex-direction: column;
	padding: 1rem 1.25rem;
	border-radius: 0.5rem;
	box-shadow: 0 0.625rem 1.875rem -0.9375rem var(--box-bg-shadow);
	animation-duration: 0.5s;
	transition: all 0.2s ease-in-out 0s;
	visibility: hidden;

	&:hover {
		box-shadow: 0 0 2rem var(--box-bg-shadow);
	}

	&:not(:first-child) {
		margin-top: 1.25rem;
	}
}

.talk-meta {
	display: flex;
	align-items: center;
	width: 100%;
}

.talk-info {
	display: flex;
	flex-direction: column;
	margin-left: 0.5rem;
}

.user-avatar {
	width: 2.8rem;
	height: 2.8rem;
	border-radius: 10px;
	object-fit: cover; // 新增：头像避免拉伸
}

// 说说内容：补充间距，和图片区隔开
.talk-content {
	margin: 0.8rem 0 0.5rem 0; // 上下间距，和头像、图片区隔开
	line-height: 1.5;
	word-wrap: break-word;
}

// ########## 核心：图片容器样式（实现一行多列、自动换行、间距）##########
.talk-image {
	display: flex;
	flex-wrap: wrap;    // 一行放不下自动换行
	gap: 4px;           // 图片之间4px小间距，和管理端一致
	align-items: flex-start; // 图片顶部对齐，避免不同高度留空白
	width: 100%;        // 占满父容器宽度
}

// ########## 核心：单张图片样式（最大高度200px、自适应、不拉伸）##########
.image {
	max-height: 200px !important; // 最大高度200px，超出等比例缩小
	width: auto !important;       // 宽度按原始比例自适应
	height: auto !important;      // 高度跟随比例，不固定
	border-radius: 0.25rem;       // 圆角，和管理端一致
	cursor: pointer;              // 鼠标小手，提示可点击预览
	flex-shrink: 0;               // 防止图片被挤压，保证一行多列
	object-fit: contain;          // 完整显示，不拉伸、不裁剪
	object-position: center;      // 比例不符时居中显示
}

// 说说信息（点赞/评论）：补充间距、样式
.info {
	display: flex;
	align-items: center;
	color: #666;
	font-size: 0.9rem;

	&.talk-like, &.talk-comment {
		margin-right: 1.5rem; // 点赞和评论之间留间距
	}
}

// 加载更多按钮容器
.loading-warp {
	text-align: center;
	margin-top: 2rem;
}
</style>
