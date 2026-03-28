package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 轮播图Request
 *
 * @author Aric
 * @date 2024/02/07 10:13
 **/
@Data
@ApiModel(value = "轮播图Request")
public class CarouselReqVo {

    /**
     * 轮播图id
     */
    @ApiModelProperty(value = "轮播图id")
    private Integer id;

    /**
     * 轮播图地址
     */
    @NotBlank(message = "imgUrl is null")
    @ApiModelProperty(value = "轮播图地址", required = true)
    private String imgUrl;

    /**
     * 是否显示 (0否 1是)
     */
    @NotNull(message = "status is null")
    @ApiModelProperty(value = "是否显示 (0否 1是)", required = true)
    private Integer status;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}