package com.furinafans.stusys.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class ScorePageDto extends BasePageQuery<Score> {

    @NotNull(message = "scId不能为空！")
    @TableField("student_course_id")
    private Long scId;

    private String examType;

    public Score toEntity() {
        return Score.builder()
                .scId(this.scId)
                .examType(this.examType)
                .build();
    }
}
