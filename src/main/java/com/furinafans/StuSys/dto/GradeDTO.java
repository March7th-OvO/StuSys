package com.furinafans.stusys.dto;

import com.furinafans.stusys.entity.Grade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class GradeDTO {
    @NotBlank(message = "Name不能为空")
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;



    public Grade toEntity(){
        return Grade.builder()
        .name(this.name.strip())
        .build();
    }
}
