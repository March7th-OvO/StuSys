package com.furinafans.stusys.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ScoreDTO {
    private BigDecimal score;
    @NotNull (message = "课程id不可为空")
    private Integer courseId;
    @NotNull(message = "学生id不可为空") 
    private Integer studentId;
    @NotBlank (message = "学年不能为空")
    private String academicYear;
    @NotBlank (message = "学期不能为空")
    private String term;
    @NotBlank (message = "考试类型不能为空")
    private String examType;
}
