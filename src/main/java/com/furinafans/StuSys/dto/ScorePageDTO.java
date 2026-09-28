package com.furinafans.stusys.dto;

import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Score;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScorePageDTO extends BasePageQuery<Score> {

    @NotNull(message = "学生id不可为空")
    private Long studentId;

    private String academicYear;

    private String term;

    private String examType;

    private Long courseId;

    public Score toEntity() {
        return Score.builder()
                .studentId(this.studentId)
                .academicYear(this.academicYear)
                .term(this.term)
                .examType(this.examType)
                .courseId(this.courseId)
                .build();
    }
}
