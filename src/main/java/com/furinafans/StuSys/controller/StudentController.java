package com.furinafans.stusys.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor 
public class StudentController {
    
    private final StudentService studentService;
    
    @PostMapping
    public Result<Integer> addStudent(@RequestBody Student student) {
        return Result.success(studentService.addStudent(student));
    }
    
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Integer id) {
        return Result.success(studentService.getStudentById(id));
    }

    @GetMapping
    public Result<IPage<Student>> page(
        @RequestParam(defaultValue = "1") Integer pageNum, 
        @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(studentService.page(pageNum, pageSize));
    }
}
