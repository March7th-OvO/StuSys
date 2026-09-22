package com.furinafans.stusys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data 
@TableName("courses")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String code;
}
