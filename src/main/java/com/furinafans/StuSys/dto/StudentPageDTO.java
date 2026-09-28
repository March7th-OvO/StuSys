package com.furinafans.stusys.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Student;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
@EqualsAndHashCode (callSuper = true)
public class StudentPageDTO extends BasePageQuery<Student>{
    private Long classId;
    
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    @Size(max = 20, message = "Number的长度不能超过20个字符")
    private String number;

    private Long gradeId;

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
    }
    
    public void setNumber(String number) {
        this.number = number == null ? null : number.strip();
    }

    public Student toEntity(){
        return Student.builder()
        .clazzId(this.classId)
        .name(this.name)
        .number(this.number)
        .build();
    }
}
