package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "任务池请求")
public class TaskPoolReq {

    @ApiModelProperty(value = "ID（更新时传）")
    private Integer id;

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty(value = "任务标题", required = true)
    private String title;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "分类")
    private String category;

    @ApiModelProperty(value = "优先级 0低 1中 2高")
    private Integer priority;
}
