package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * ThinkingReq
 *
 * @author Aric
 */
@Data
@ApiModel(description = "思考沉淀请求")
public class ThinkingReq {

    @ApiModelProperty(value = "ID（更新时传）")
    private Integer id;

    @NotBlank(message = "思考主题不能为空")
    @ApiModelProperty(value = "思考主题", required = true)
    private String topic;

    @ApiModelProperty(value = "分类")
    private String category;

    @NotBlank(message = "收获不能为空")
    @ApiModelProperty(value = "收获", required = true)
    private String harvest;

    @ApiModelProperty(value = "备注")
    private String remark;
}