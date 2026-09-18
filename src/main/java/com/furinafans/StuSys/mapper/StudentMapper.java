package com.furinafans.stusys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.furinafans.stusys.entity.Student;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}