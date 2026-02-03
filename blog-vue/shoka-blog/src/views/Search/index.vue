<template>
  <div class="page-header">
    <h1 class="page-title">搜索结果</h1>
    <img class="page-cover" src="https://big-event0611.oss-cn-beijing.aliyuncs.com/article/259530df534ba373663bf762e5dc3d36.jpg" alt="">
    <Waves></Waves>
  </div>
  <div class="bg">
    <div class="main-container mt">
      <div class="left-container">
        <div class="article-item" v-animate="['slideUpBigIn']" v-for="article of articleList" :key="article.id">
          <!-- 文章缩略图 -->
          <div class="article-cover">
            <router-link :to="`/article/${article.id}`" href="">
              <img class="cover" v-lazy="article.articleCover" />
            </router-link>
          </div>
          <!-- 文章信息 -->
          <div class="article-info">
            <div class="article-meta">
              <!-- 置顶 -->
              <span class="top" v-if="article.isTop == 1">
                <svg-icon icon-class="top" size="0.85rem" style="margin-right: 0.15rem"></svg-icon>置顶</span>
              <!-- 发表时间 -->
              <span class="meta-item ml-3.75">
                <svg-icon icon-class="calendar" size="0.9rem" style="margin-right: 0.15rem"></svg-icon>{{
                formatDate(article.createTime) }}
              </span>
              <!-- 文章标签 -->
              <router-link class="meta-item ml-3.75" :to="`/tag/${tag.id}`" v-for="tag in article.tagVOList" :key="tag.id">
                <svg-icon icon-class="tag" size="0.9rem" style="margin-right: 0.15rem"></svg-icon>{{ tag.tagName }}
              </router-link>
            </div>
            <!-- 文章标题 -->
            <h3 class="article-title">
              <router-link :to="`/article/${article.id}`" v-html="article.articleTitle">
              </router-link>
            </h3>
            <!-- 文章内容 -->
            <div class="article-content" v-html="article.articleContent"></div>
            <!-- 文章分类 -->
            <div class="article-category">
              <svg-icon icon-class="qizhi" size="0.85rem" style="margin-right: 0.15rem"></svg-icon>
              <router-link :to="`/category/${article.category.id}`">{{
                article.category.categoryName
                }}</router-link>
            </div>
            <!-- 阅读按钮 -->
            <router-link class="article-btn" :to="`/article/${article.id}`">more...</router-link>
          </div>
        </div>
        <div v-if="articleList.length === 0" class="no-result">
          没有找到相关文章
        </div>
      </div>
      <SideBar class="right-container"></SideBar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { searchArticle } from "@/api/article";
import { ArticleSearch } from "@/api/article/types";
import SideBar from "@/components/Layout/SideBar/index.vue";
import { formatDate } from "@/utils/date";

const route = useRoute();
const articleList = ref<ArticleSearch[]>([]);
const keyword = ref("");

const handleSearch = () => {
  keyword.value = route.query.keyword as string;
  if (keyword.value) {
    searchArticle(keyword.value).then(({ data }) => {
      articleList.value = data.data;
    });
  } else {
    articleList.value = [];
  }
};

watch(
  () => route.query.keyword,
  () => {
    handleSearch();
  }
);

onMounted(() => {
  handleSearch();
});
</script>

<style lang="scss" scoped>
.mt {
  margin-top: 1rem;
  padding-bottom: 1.75rem;
}

.article-item {
  display: flex;
  height: 14rem;
  margin: 1.25rem 0.5rem 0;
  border-radius: 0.5rem;
  box-shadow: 0 0.625rem 1.875rem -0.9375rem var(--box-bg-shadow);
  animation-duration: 0.5s;
  transition: all 0.2s ease-in-out 0s;
  visibility: hidden;

  &:hover {
    box-shadow: 0 0 1.5rem var(--box-bg-shadow);

    .cover {
      transform: scale(1.05) rotate(1deg);
    }
  }

  &:nth-child(even) {
    flex-direction: row-reverse;

    .article-cover {
      margin-right: auto;
      margin-left: 1.5rem;
      -webkit-clip-path: polygon(0 0, 100% 0, 100% 100%, 8% 100%);
      clip-path: polygon(0 0, 100% 0, 100% 100%, 8% 100%);
      border-radius: 0 0.625rem 0.625rem 0;
    }

    .article-info {
      padding: 1rem 0 3rem 1.5rem;

      .article-meta {
        justify-content: flex-start;
      }

      .article-category {
        justify-content: flex-start;
        right: auto;
        left: 0;
      }
    }
  }
}

.article-cover {
  width: 45%;
  height: 100%;
  overflow: hidden;
  margin-right: 1.5rem;
  -webkit-clip-path: polygon(0 0, 92% 0, 100% 100%, 0 100%);
  clip-path: polygon(0 0, 92% 0, 100% 100%, 0 100%);
  border-radius: 0.625rem 0 0 0.625rem;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.2s ease-in-out 0s;
}

.article-info {
  position: relative;
  width: 50%;
  padding: 1rem 1.5rem 3rem 0;
  perspective: 62.5rem;
}

.article-meta {
  display: flex;
  justify-content: flex-end;
  font-size: 0.8rem;
  color: #888;
}

.top {
  color: #ff7242;
  margin-left: 0.625rem;
}

.meta-item {
  display: flex;
  align-items: center;
}

.ml-3\.75 {
  margin-left: 0.9375rem;
}

.article-title {
  margin-top: 0.625rem;
  margin-bottom: 0.625rem;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;

  a {
    transition: all 0.2s ease-in-out 0s;

    &:hover {
      color: #49b1f5;
    }
  }
}

.article-content {
  font-size: 0.9rem;
  line-height: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.article-category {
  position: absolute;
  display: flex;
  align-items: center;
  bottom: 0.5rem;
  font-size: 0.85rem;
  color: #858585;
  transition: all 0.2s ease-in-out 0s;

  &:hover {
    color: #49b1f5;
  }
}

.article-btn {
  position: absolute;
  bottom: 0.5rem;
  right: 0;
  padding: 0 0.8rem;
  border-radius: 1rem;
  color: #fff;
  background-image: linear-gradient(to right, #ed6ea0 0, #ec8c69 100%);
  z-index: 1;

  &:hover {
    transform: translateZ(2.5rem);
  }
}

.no-result {
  text-align: center;
  margin-top: 2rem;
  font-size: 1.2rem;
  color: #666;
}
</style>
