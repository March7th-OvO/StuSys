package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.entity.Student;

public interface StudentService {
    Student addStu(Student student);

    void delete(String number);

    Student updateStu(String number, Student student);

    Student searchStuByNum(String number);

    IPage<Student> page(Integer pageNum, Integer pageSize);
}
