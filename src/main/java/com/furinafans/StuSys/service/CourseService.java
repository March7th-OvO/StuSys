package com.furinafans.stusys.service;

import com.furinafans.stusys.entity.Course;

public interface CourseService {
    Integer addCourse(Course course);
    void delCourse(String name);
    void updateCourse(String name, Course course);
}
