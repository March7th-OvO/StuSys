package com.furinafans.stusys.dto;

import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Course;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoursePageDTO extends BasePageQuery<Course> {
    
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    public Course toEntity() {
        return Course.builder()
                .name(this.name)
                .build();
    }
}
