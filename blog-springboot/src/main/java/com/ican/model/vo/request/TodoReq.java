package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "??????")
public class TodoReq {

    @ApiModelProperty(value = "ID")
    private Integer id;

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty(value = "标题", required = true)
    private String title;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "优先级 0低 1中 2高")
    private Integer priority;

    @ApiModelProperty(value = "分类标签")
    private String category;

    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @ApiModelProperty(value = "截止时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty(value = "类型 0待办 1任务池")
    private Integer type;
}
