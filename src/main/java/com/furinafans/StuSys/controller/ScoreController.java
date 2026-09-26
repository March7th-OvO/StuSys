package com.furinafans.stusys.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.dto.ScoreDTO;
import com.furinafans.stusys.entity.Score;
import com.furinafans.stusys.service.ScoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
@Validated 
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping
    public Result<Score> addScore(@RequestBody @Valid ScoreDTO scoreDTO) {
        return Result.success(scoreService.addScore(scoreDTO.toEntity()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delScore(@PathVariable Long id) {
        return Result.success(scoreService.delScore(id));
    }

    @PutMapping("/{id}")
    public Result<Score> updateScore(@PathVariable Long id, @RequestBody @Valid ScoreDTO scoreDTO) {
        return Result.success(scoreService.updateScore(id, scoreDTO.toEntity()));
    }

    @GetMapping("/{number}")
    public Result<IPage<Score>> getScoreByStuNum(Page<Score> page, @PathVariable String number) {
        return Result.success(scoreService.selectScoreByStuNum(page, number));
    }
}
