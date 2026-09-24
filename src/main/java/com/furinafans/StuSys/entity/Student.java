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
@NoArgsConstructor 
@AllArgsConstructor 
@TableName("students")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private String name;
    private String number;
}
