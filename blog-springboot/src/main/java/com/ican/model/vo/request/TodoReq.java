package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "??????")
public class TodoReq {

    @ApiModelProperty(value = "??ID")
    private Integer id;

    @NotBlank(message = "??????")
    @ApiModelProperty(value = "??", required = true)
    private String title;

    @ApiModelProperty(value = "????")
    private String description;

    @ApiModelProperty(value = "??? 0? 1? 2?")
    private Integer priority;

    @ApiModelProperty(value = "????")
    private String category;

    @ApiModelProperty(value = "???? yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @ApiModelProperty(value = "???? yyyy-MM-dd HH:mm:ss")
    private String endTime;
}
