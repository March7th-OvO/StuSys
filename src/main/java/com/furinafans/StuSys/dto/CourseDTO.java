package com.furinafans.stusys.dto;

import com.furinafans.stusys.entity.Course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    @NotBlank(message = "Name不能为空")
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    @NotBlank(message = "Code不能为空")
    @Size(max = 20, message = "Code的长度不能超过20个字符")
    private String code;

    public Course toEntity() {
        return Course.builder()
                .name(this.name.strip())
                .code(this.code.strip())
                .build();
    }
}
