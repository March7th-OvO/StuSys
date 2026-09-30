package com.furinafans.stusys.vo;

import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.StuCou;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StuCouVo extends BasePageQuery<StuCou> {
    private Long id;
    private Integer status;
    private Double score;
    private Long studentId;
    private Long courseId;
    private String academicYear;
    private Integer term;

    public StuCou toEntity() {
        return StuCou.builder()
                .id(this.id)
                .status(this.status)
                .studentId(this.studentId)
                .courseId(this.courseId)
                .academicYear(this.academicYear)
                .term(this.term)
                .build();
    }
}
