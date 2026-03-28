package com.ican.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TimeBlock
 *
 * @author Aric
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeBlock {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private LocalDate blockDate;

    private String name;

    private String category;

    /** HH:mm 格式，如 "08:30" */
    private String startTime;

    /** HH:mm 格式，如 "10:00" */
    private String endTime;

    private String remark;

    /** 颜色 hex，如 "#10b981" */
    private String color;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}