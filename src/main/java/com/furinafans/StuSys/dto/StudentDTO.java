package com.furinafans.stusys.dto;

import com.furinafans.stusys.entity.Student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {

    @NotNull(message = "ClassId不能为空")
    @Positive(message = "ClassId必须大于0")
    private Long classId;

    @NotBlank(message = "Name不能为空")
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    @NotBlank(message = "Number不能为空")
    @Size(max = 20, message = "Number的长度不能超过20个字符")
    private String number;

    public Student toEntity() {
        return Student.builder()
                .classId(this.classId)
                .name(this.name.strip())
                .number(this.number.strip())
                .build();
    }
}
