package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.dto.ScorePageDTO;
import com.furinafans.stusys.entity.Score;

public interface ScoreService {
    Score addScore(Score score);

    void delScore(Long id);

    Score updateScore(Long id, Score score);

    IPage<Score> page(ScorePageDTO pageDTO);
}
