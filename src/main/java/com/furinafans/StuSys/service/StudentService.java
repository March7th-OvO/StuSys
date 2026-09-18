package com.furinafans.stusys.service;

import java.util.List;

import com.furinafans.stusys.entity.Student;

public interface StudentService {
    List<Student> listAll();
    void addStudent(Student student);
    Student getStudentById(Integer id);
}
