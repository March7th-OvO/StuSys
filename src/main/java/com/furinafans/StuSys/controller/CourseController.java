package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.dto.CourseDTO;
import com.furinafans.stusys.entity.Course;
import com.furinafans.stusys.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/courses")
@RequiredArgsConstructor 
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public Result<Integer> addCourse(@RequestBody @Valid CourseDTO courseDTO){
        Course course = Course.builder()
        .name(courseDTO.getName())
        .code(courseDTO.getCode())
        .build();
        return Result.success(courseService.addCourse(course));
    }

    @DeleteMapping("/{name}")
    public Result<Void> delCourse(@PathVariable("name") String name){
        courseService.delCourse(name);
        return Result.success();
    }

    @PutMapping("/{name}")
    public Result<Void> updateCourse(@PathVariable("name") String name, @RequestBody @Valid CourseDTO courseDTO){
        Course course = Course.builder()
        .name(courseDTO.getName())
        .code(courseDTO.getCode())
        .build();
        courseService.updateCourse(name, course);
        return Result.success();
    }
}
