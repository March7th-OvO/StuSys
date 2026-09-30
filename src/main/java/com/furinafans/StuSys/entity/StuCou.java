package com.furinafans.stusys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@TableName ("student_course")
public class StuCou {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer status;
    private Double score;
    private Long studentId;
    private Long courseId;
    private String academicYear;
    private String term;
}
