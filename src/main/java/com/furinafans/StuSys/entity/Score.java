package com.furinafans.stusys.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("scores")
public class Score {
    @TableId(type = IdType.AUTO)
    @NotNull(message = "主键不能为空")
    private Long id;
    
    @NotNull(message = "scId不能为空")
    @TableField ("student_course_id")
    private Long scId;

    @PositiveOrZero(message = "分数不能小于0")
    private BigDecimal score;

    @NotBlank(message = "考试类型不能为空")
    private String examType;
}
