package com.furinafans.stusys.dto;

import java.math.BigDecimal;

import com.furinafans.stusys.entity.Score;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreDTO {

    @PositiveOrZero
    private BigDecimal score;
    @NotNull(message = "课程id不可为空")
    private Long courseId;
    @NotNull(message = "学生id不可为空")
    private Long studentId;
    @NotBlank(message = "学年不能为空")
    private String academicYear;
    @NotBlank(message = "学期不能为空")
    private String term;
    @NotBlank(message = "考试类型不能为空")
    private String examType;

    public Score toEntity() {
        return Score.builder()
                .score(this.score)
                .courseId(this.courseId)
                .studentId(this.studentId)
                .academicYear(this.academicYear.strip())
                .term(this.term.strip())
                .examType(this.examType.strip())
                .build();
    }
}
