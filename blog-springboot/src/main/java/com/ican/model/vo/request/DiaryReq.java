package com.ican.model.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "日记请求")
public class DiaryReq {

    @ApiModelProperty(value = "日记ID（更新时传）")
    private Integer id;

    @NotNull(message = "日期不能为空")
    @ApiModelProperty(value = "日记日期 yyyy-MM-dd", required = true)
    private String diaryDate;

    @NotBlank(message = "内容不能为空")
    @ApiModelProperty(value = "日记内容", required = true)
    private String content;

    @ApiModelProperty(value = "心情标签")
    private String mood;
}
