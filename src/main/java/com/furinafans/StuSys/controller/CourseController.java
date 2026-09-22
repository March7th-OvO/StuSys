package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.entity.Course;
import com.furinafans.stusys.service.CourseService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/courses")
@RequiredArgsConstructor 
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public Result<Integer> addCourse(@RequestBody Course course){
        return Result.success(courseService.addCourse(course));
    }

    @DeleteMapping("/{name}")
    public Result<Void> delCourse(@PathVariable("name") String name){
        courseService.delCourse(name);
        return Result.success();
    }
}
