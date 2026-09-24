package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.entity.Course;

public interface CourseService {
    Long addCourse(Course course);
    void delCourse(String name);
    void updateCourse(String name, Course course);
    Course searchCourseByCode(String code);
    IPage<Course> page(Integer pageNum, Integer pageSize);
}

