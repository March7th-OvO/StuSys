package com.furinafans.stusys.service;

import java.util.List;

import com.furinafans.stusys.entity.Student;

public interface StudentService {
    List<Student> listAll();
    Integer addStudent(Student student);
    Student getStudentById(Integer id);
}
