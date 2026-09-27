package com.furinafans.stusys.dto;

import com.furinafans.stusys.entity.Clazz;

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
public class ClazzDTO {
    @NotBlank(message = "Name不能为空")
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    @NotNull (message = "年级Id不能为空")
    @Positive (message = "年级Id不能小于1")
    private Long gradeId;

    public Clazz toEntity(){
        return Clazz.builder()
        .name(this.name)
        .gradeId(this.gradeId)
        .build();
    }
}
