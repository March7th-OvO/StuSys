package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.dto.StuCouPageDto;
import com.furinafans.stusys.dto.StuCouXuanDto;
import com.furinafans.stusys.entity.StuCou;
import com.furinafans.stusys.service.StuCouService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/student-course")
@RequiredArgsConstructor 
public class StudentCourseController {

    private final StuCouService scService;
    
    @PostMapping 
    public Result<StuCou> xuanKe(@RequestBody @Valid StuCouXuanDto sc){
        return Result.success(scService.xuanKe(sc));
    }

    @PutMapping ("/tuike/{id}")
    public Result<Void> tuiKe(@PathVariable Long id){
        scService.tuiKe(id);
        return Result.success();
    }

    @PutMapping ("/jieke/{id}")
    public Result<Void> jieKe(@PathVariable Long id){
        scService.jieKe(id);
        return Result.success();
    }

    @GetMapping 
    public Result<IPage<StuCou>> page(@Valid StuCouPageDto request){
        return Result.success(scService.page(request));
    }
}
