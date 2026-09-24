package com.furinafans.stusys.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("Scores")
public class Score {
    @TableId(type = IdType.AUTO) 
    @NotNull (message = "主键不能为空")
    private Long id;

    @PositiveOrZero (message = "分数不能小于0")
    private BigDecimal score;

    @NotNull (message = "课程不能为空")
    private Long courseId;

    @NotNull (message = "学生不能为空")
    private Long studentId;

    @NotBlank (message = "学年不能为空")
    private String academicYear;
    
    @NotBlank (message = "学期不能为空")
    private String term;
    
    @NotBlank (message = "考试类型不能为空")
    private String examType;
}
