package com.ican.model.vo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "代办查询条件")
public class TodoQuery extends PageQuery {

    @ApiModelProperty(value = "状态 0未完成 1已完成")
    private Integer status;

    @ApiModelProperty(value = "优先级 0低 1中 2高")
    private Integer priority;

    @ApiModelProperty(value = "分类标签")
    private String category;

    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "开始日期 yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty(value = "结束日期 yyyy-MM-dd")
    private String endDate;
}
