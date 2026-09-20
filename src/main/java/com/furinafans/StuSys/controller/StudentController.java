package com.furinafans.stusys.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor 
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping()
    public Result<List<Student>> listAll() {
        return Result.success(studentService.listAll());
    }
    
    @PostMapping()
    public Result<Integer> addStudent(@RequestBody Student student) {
        return Result.success(studentService.addStudent(student));
    }
    
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Integer id) {
        return Result.success(studentService.getStudentById(id));
    }
}
