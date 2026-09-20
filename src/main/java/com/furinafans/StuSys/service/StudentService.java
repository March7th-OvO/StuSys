package com.furinafans.stusys.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.entity.Student;

public interface StudentService {
    Integer addStudent(Student student);
    Student getStudentById(Integer id);
    IPage<Student> page(Integer pageNum, Integer pageSize);
}
