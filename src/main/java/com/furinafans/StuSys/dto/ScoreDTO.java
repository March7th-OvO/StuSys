package com.furinafans.stusys.dto;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
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

    @NotNull (message = "scId不能为空！")
    @TableField ("student_course_id")
    private Long scId;

    @PositiveOrZero(message = "考试成绩不能小于0！")
    private BigDecimal score;

    @NotBlank(message = "考试类型不能为空！")
    private String examType;

    public Score toEntity() {
        return Score.builder()
                .scId(this.scId)
                .score(this.score)
                .examType(this.examType.strip())
                .build();
    }
}
