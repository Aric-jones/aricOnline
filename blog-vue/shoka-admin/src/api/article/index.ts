import { PageResult, Result } from "@/model";
import request from "@/utils/request";
import { AxiosPromise } from "axios";
import {
  Article,
  ArticleForm,
  ArticleQuery,
  CategoryVO,
  DeleteDTO,
  Recommend,
  TagVO,
  Top,
} from "./types";

/**
 * 查看文章列表
 * @param params 查询条件
 * @returns 文章列表
 */
export function getArticleList(params: ArticleQuery): AxiosPromise<Result<PageResult<Article[]>>> {
  return request({
    url: "/admin/article/list",
    method: "get",
    params,
  });
}

/**
 * 获取分类选项
 * @returns 分类选项
 */
export function getCategoryOption(): AxiosPromise<Result<CategoryVO[]>> {
  return request({
    url: "/admin/category/option",
    method: "get",
  });
}

/**
 * 获取标签选项
 * @returns 标签选项
 */
export function getTagOption(): AxiosPromise<Result<TagVO[]>> {
  return request({
    url: "/admin/tag/option",
    method: "get",
  });
}

/**
 * 添加文章
 * @param data 文章信息
 */
export function addArticle(data: ArticleForm): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/add",
    method: "post",
    data,
  });
}

/**
 * 物理删除文章
 * @param data 文章id
 */
export function deleteArticle(data: number[]): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/delete",
    method: "delete",
    data,
  });
}

/**
 * 逻辑删除文章
 * @param data 逻辑删除
 */
export function recycleArticle(data: DeleteDTO): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/recycle",
    method: "put",
    data,
  });
}

/**
 * 修改文章
 * @param data 文章信息
 */
export function updateArticle(data: ArticleForm): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/update",
    method: "put",
    data,
  });
}

/**
 * 编辑文章
 * @param articleId 文章id
 */
export function editArticle(articleId: number): AxiosPromise<Result<ArticleForm>> {
  return request({
    url: `/admin/article/edit/${articleId}`,
    method: "get",
  });
}

/**
 * 修改文章置顶状态
 * @param data 置顶信息
 */
export function updateArticleTop(data: Top): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/top",
    method: "put",
    data,
  });
}

/**
 * 修改文章推荐状态
 * @param data 推荐信息
 */
export function updateArticleRecommend(data: Recommend): AxiosPromise<Result<null>> {
  return request({
    url: "/admin/article/recommend",
    method: "put",
    data,
  });
}

/**
 * 上传文章图片
 * @returns 图片链接
 */
export function uploadArticleCover(data: FormData): AxiosPromise<Result<string>> {
  return request({
    url: "/admin/article/upload",
    headers: { "content-type": "multipart/form-data" },
    method: "post",
    data,
  });
}

/**
 * AI 生成文章摘要
 * @param content 文章内容
 * @returns 摘要文本
 */
export function generateAiSummary(content: string): AxiosPromise<Result<string>> {
  return request({
    url: "/admin/ai/summary",
    method: "post",
    timeout: 60000,
    data: { content },
  });
}

/**
 * AI 生成文章标题
 * @param content 文章内容
 * @returns 标题文本
 */
export function generateAiTitle(content: string): AxiosPromise<Result<string>> {
  return request({
    url: "/admin/ai/title",
    method: "post",
    timeout: 60000,
    data: { content },
  });
}

/**
 * AI 自动选择分类
 * @param content 文章内容
 * @param categories 已有分类（逗号分隔）
 * @returns 分类名称
 */
export function generateAiCategory(content: string, categories: string): AxiosPromise<Result<string>> {
  return request({
    url: "/admin/ai/category",
    method: "post",
    timeout: 60000,
    data: { content, categories },
  });
}

/**
 * AI 自动选择标签
 * @param content 文章内容
 * @param tags 已有标签（逗号分隔）
 * @returns 标签名列表
 */
export function generateAiTags(content: string, tags: string): AxiosPromise<Result<string[]>> {
  return request({
    url: "/admin/ai/tags",
    method: "post",
    timeout: 60000,
    data: { content, tags },
  });
}
