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

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping
    public Result<Boolean> addScore(@RequestBody @Valid ScoreDTO scoreDTO) {
        Score score = Score.builder()
                .score(scoreDTO.getScore())
                .courseId(scoreDTO.getCourseId())
                .studentId(scoreDTO.getStudentId())
                .academicYear(scoreDTO.getAcademicYear())
                .term(scoreDTO.getTerm())
                .examType(scoreDTO.getExamType())
                .build();

        return Result.success(scoreService.addScore(score));
    }

    @DeleteMapping
    public Result<Boolean> delScore(@RequestBody Score score) {
        return Result.success(scoreService.delScore(score));
    }

    @PutMapping
    public Result<Void> updateScore(@RequestBody @Valid Score score) {
        scoreService.updateScore(score);
        return Result.success();
    }

    @GetMapping("/{number}")
    public Result<IPage<Score>> getScoreByStuNum(Page<Score> page, @PathVariable String number) {
        return Result.success(scoreService.selectScoreByStuNum(page, number));
    }
}
