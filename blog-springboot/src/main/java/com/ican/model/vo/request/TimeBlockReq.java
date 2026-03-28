package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TimeBlockReq
 *
 * @author Aric
 */
@Data
@ApiModel(description = "时间块请求")
public class TimeBlockReq {

    @ApiModelProperty(value = "ID（更新时传）")
    private Integer id;

    @NotBlank(message = "名称不能为空")
    @ApiModelProperty(value = "事件名称", required = true)
    private String name;

    @NotBlank(message = "分类不能为空")
    @ApiModelProperty(value = "分类", required = true)
    private String category;

    @NotNull(message = "日期不能为空")
    @ApiModelProperty(value = "日期 yyyy-MM-dd", required = true)
    private String blockDate;

    @NotBlank(message = "开始时间不能为空")
    @ApiModelProperty(value = "开始时间 HH:mm", required = true)
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @ApiModelProperty(value = "结束时间 HH:mm", required = true)
    private String endTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "颜色")
    private String color;
}