import { PageQuery, PageResult, Result } from "@/model";
import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { Article, ArticleInfo, ArticleRank, ArticleRecommend, ArticleSearch } from "./types";

/**
 * 查看文章列表
 * @param params 查询条件
 * @returns 文章列表
 */
export function getArticleList(params: PageQuery): AxiosPromise<Result<PageResult<Article[]>>> {
  return request({
    url: "/article/list",
    method: "get",
    params,
  });
}

/**
 * 查看文章
 * @param articleId 文章id
 */
export function getArticle(articleId: number): AxiosPromise<Result<ArticleInfo>> {
  return request({
    url: `/article/${articleId}`,
    method: "get",
  });
}

/**
 * 查看推荐文章
 * @returns 推荐文章
 */
export function getArticleRecommend(): AxiosPromise<Result<ArticleRecommend[]>> {
  return request({
    url: "/article/recommend",
    method: "get",
  });
}

/**
 * 查看文章浏览量排行（前5篇）
 * @returns 文章排行
 */
export function getArticleRank(): AxiosPromise<Result<ArticleRank[]>> {
  return request({
    url: "/article/rank",
    method: "get",
  });
}

/**
 * 搜索文章
 * @returns 文章列表
 */
export function searchArticle(keyword: string): AxiosPromise<Result<ArticleSearch[]>> {
  return request({
    url: "/article/search",
    method: "get",
    params: {
      keyword,
    },
  });
}

/**
 * 点赞文章
 * @param articleId 文章id
 */
export function likeArticle(articleId: number): AxiosPromise<Result<null>> {
  return request({
    url: `/article/${articleId}/like`,
    method: "post",
  });
}

/**
 * AI 快速阅读
 * @param content 文章内容
 * @returns 200字概要
 */
export function quickReadArticle(content: string): AxiosPromise<Result<string>> {
  return request({
    url: "/ai/quick-read",
    method: "post",
    timeout: 60000,
    data: { content },
  });
}

/**
 * 每日推荐文章
 * @returns 推荐文章
 */
export function getDailyArticle(): AxiosPromise<Result<ArticleRecommend>> {
  return request({
    url: "/article/daily",
    method: "get",
  });
}
