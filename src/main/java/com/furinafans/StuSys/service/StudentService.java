package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.entity.Student;

public interface StudentService {

    
    Integer addStudent(Student student);
    
    /**
     * 根据学号查询学生信息
     * @param number 学号
     * @return 学生对象
     */
    Student getStudentByNum(String number);

    IPage<Student> page(Integer pageNum, Integer pageSize);

    void delete(String number);

    /**
     * 根据学号更新学生信息
     * @param number  学号（路径变量/唯一定位标识）
     * @param student 提交的最新学生数据
     */
    void updateStudent(String number, Student newStudent);
}
