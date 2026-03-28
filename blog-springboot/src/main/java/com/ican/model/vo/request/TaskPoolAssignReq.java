package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TaskPoolAssignReq
 *
 * @author Aric
 */
@Data
@ApiModel(description = "任务分配到周请求")
public class TaskPoolAssignReq {

    @NotNull(message = "任务ID不能为空")
    @ApiModelProperty(value = "任务ID", required = true)
    private Integer id;

    @NotBlank(message = "周起始日期不能为空")
    @ApiModelProperty(value = "周一日期 yyyy-MM-dd", required = true)
    private String weekStartDate;
}