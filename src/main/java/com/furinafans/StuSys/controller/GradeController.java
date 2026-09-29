package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.dto.GradeDTO;
import com.furinafans.stusys.entity.Grade;
import com.furinafans.stusys.service.GradeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public Result<Grade> addGrade(@RequestBody @Valid GradeDTO gradeDTO) {
        return Result.success(gradeService.addGrade(gradeDTO.toEntity()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delGrade(@PathVariable("id") Long id) {
        gradeService.delGrade(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Grade> updateGrade(
            @PathVariable("id") Long id,
            @RequestBody @Valid GradeDTO gradeDTO) {
        Grade grade = gradeDTO.toEntity();
        grade.setId(id);
        return Result.success(gradeService.updateGrade(grade));
    }

    @GetMapping("/{id}")
    public Result<Grade> selectGrade(@PathVariable("id") Long id) {
        return Result.success(gradeService.selectGrade(id));
    }

    @GetMapping
    public Result<IPage<Grade>> page(@Valid BasePageQuery<Grade> request) {
        return Result.success(gradeService.page(request));
    }
}
