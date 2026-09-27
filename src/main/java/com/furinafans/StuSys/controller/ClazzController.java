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
import com.furinafans.stusys.dto.ClazzDTO;
import com.furinafans.stusys.dto.ClazzPageDTO;
import com.furinafans.stusys.entity.Clazz;
import com.furinafans.stusys.service.ClazzService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClazzController {
    private final ClazzService clazzService;

    @PostMapping
    public Result<Clazz> addClazz(@RequestBody @Valid ClazzDTO clazzDto) {
        return Result.success(clazzService.addClazz(clazzDto.toEntity()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delClazz(@PathVariable("id") Long id) {
        clazzService.delClazz(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Clazz> updateClazz(@PathVariable("id")  Long id, @RequestBody @Valid ClazzDTO clazzDto) {
        Clazz clazz = clazzDto.toEntity();
        clazz.setId(id);
        return Result.success(clazzService.updateClazz(clazz));
    }

    @GetMapping("/{id}")
    public Result<Clazz> selectClazz(@PathVariable("id") Long id) {
        return Result.success(clazzService.selectClazz(id));
    }

    @GetMapping
    public Result<IPage<Clazz>> pageClazz(@Valid ClazzPageDTO pageDTO) {
        return Result.success(clazzService.pageClazz(pageDTO));
    }
}
