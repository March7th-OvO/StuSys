package com.furinafans.stusys.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.dto.CoursePageDTO;
import com.furinafans.stusys.entity.Course;

public interface CourseService {
    Course addCourse(Course course);

    void delCourse(Long id);

    Course updateCourse(String name, Course course);

    Course searchCourseByCode(String code);

    IPage<Course> page(CoursePageDTO pageDTO);
}
