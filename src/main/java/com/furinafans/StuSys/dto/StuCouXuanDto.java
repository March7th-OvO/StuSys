package com.furinafans.stusys.dto;

import com.furinafans.stusys.entity.StuCou;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class StuCouXuanDto {
    @NotNull (message = "学生ID不能为空！")
    private Long studentId;

    @NotNull (message = "课程ID不能为空！")
    private Long courseId;

    @NotBlank (message = "学年不能为空！")
    private String academicYear;

    @NotBlank (message = "学期不能为空！")
    private String term;

    public StuCou toEntity(){
        return StuCou.builder()
        .studentId(this.studentId)
        .courseId(this.courseId)
        .academicYear(this.academicYear)
        .term(this.term)
        .build();
    }
}
