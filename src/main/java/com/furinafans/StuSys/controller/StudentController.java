package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.dto.StudentDTO;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public Result<Student> addStudent(@RequestBody @Valid StudentDTO stuDTO) {
        return Result.success(studentService.addStu(stuDTO.toEntity()));
    }

    @DeleteMapping("/{number}")
    public Result<Void> delete(@PathVariable("number") String number) {
        studentService.delete(number);
        return Result.success();
    }

    @PutMapping("/{number}")
    public Result<Student> update(@PathVariable("number") String number, @RequestBody @Valid StudentDTO stuDTO) {
        return Result.success(studentService.updateStu(number, stuDTO.toEntity()));
    }

    @GetMapping("/{number}")
    public Result<Student> getStudentById(@PathVariable("number") String number) {
        return Result.success(studentService.searchStuByNum(number));
    }

    @GetMapping
    public Result<IPage<Student>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.page(pageNum, pageSize));
    }
}
