package com.ican.model.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章条件Response
 *
 * @author ican
 */
@Data
@ApiModel(description = "文章条件Response")
public class ArticleConditionResp {

    /**
     * 文章id
     */
    @ApiModelProperty(value = "文章id")
    private Integer id;

    /**
     * 文章缩略图
     */
    @ApiModelProperty(value = "文章缩略图")
    private String articleCover;

    /**
     * 文章标题
     */
    @ApiModelProperty(value = "文章标题")
    private String articleTitle;

    /**
     * 文章摘要（列表预览用）
     */
    @ApiModelProperty(value = "文章摘要")
    private String articleDesc;

    /**
     * 文章内容（列表预览片段，与搜索页一致）
     */
    @ApiModelProperty(value = "文章内容")
    private String articleContent;

    /**
     * 是否置顶 (0否 1是)
     */
    @ApiModelProperty(value = "是否置顶")
    private Integer isTop;

    /**
     * 文章分类
     */
    @ApiModelProperty(value = "文章分类")
    private CategoryOptionResp category;

    /**
     * 文章标签
     */
    @ApiModelProperty(value = "文章标签")
    private List<TagOptionResp> tagVOList;

    /**
     * 发表时间
     */
    @ApiModelProperty(value = "发表时间")
    private LocalDateTime createTime;

}
