package com.ican.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * B站IP地址返回DTO
 *
 * @author ican
 * @date 2024/06/21 22:52
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "B站IP地址返回DTO")
public class BiliIpInfoDTO {

    /**
     * 状态码
     */
    @ApiModelProperty(value = "状态码")
    private Integer code;

    /**
     * 提示信息
     */
    @ApiModelProperty(value = "提示信息")
    private String message;

    /**
     * 提示信息（部分接口用msg，兼容）
     */
    @ApiModelProperty(value = "提示信息")
    private String msg;

    /**
     * 返回数据：接口实际返回数组，改为List集合
     */
    @ApiModelProperty(value = "返回数据")
    private List<IpInfoData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "返回数据")
    public static class IpInfoData {

        /**
         * ip地址
         */
        @ApiModelProperty(value = "ip地址")
        private String addr;

        /**
         * 国家
         */
        @ApiModelProperty(value = "country")
        private String country;

        /**
         * 省份
         */
        @ApiModelProperty(value = "province")
        private String province;

        /**
         * 城市
         */
        @ApiModelProperty(value = "city")
        private String city;

        /**
         * 供应商
         */
        @ApiModelProperty(value = "供应商")
        private String isp;

        /**
         * 纬度
         */
        @ApiModelProperty(value = "纬度")
        private String latitude;

        /**
         * 经度
         */
        @ApiModelProperty(value = "经度")
        private String longitude;
    }

}