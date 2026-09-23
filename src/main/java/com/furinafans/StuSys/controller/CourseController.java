package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.dto.CourseDTO;
import com.furinafans.stusys.entity.Course;
import com.furinafans.stusys.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    //插入课程
    @PostMapping
    public Result<Integer> addCourse(@RequestBody @Valid CourseDTO courseDTO) {
        Course course = Course.builder()
                .name(courseDTO.getName().strip())
                .code(courseDTO.getCode().strip())
                .build();
        return Result.success(courseService.addCourse(course));
    }

    //根据课程名称删除
    @DeleteMapping("/{name}")
    public Result<Void> delCourse(@PathVariable("name") String name) {
        courseService.delCourse(name);
        return Result.success();
    }

    //修改课程信息
    @PutMapping("/{name}")
    public Result<Void> updateCourse(@PathVariable("name") String name, @RequestBody @Valid CourseDTO courseDTO) {
        Course course = Course.builder()
                .name(courseDTO.getName().strip())
                .code(courseDTO.getCode().strip())
                .build();
        courseService.updateCourse(name, course);
        return Result.success();
    }

    //根据code查询课程
    @GetMapping("/{code}")
    public Result<Course> searchCourseByCode(@PathVariable("code") String code) {
        return Result.success(courseService.searchCourseByCode(code));
    }

    //分页查询全部课程
    @GetMapping
    public Result<IPage<Course>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(courseService.page(pageNum, pageSize));
    }
}
