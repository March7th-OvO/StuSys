package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.entity.Score;

public interface ScoreService {
    Score addScore(Score score);

    void delScore(Long id);

    Score updateScore(Long id, Score score);

    IPage<Score> selectScoreByStuNum(Page<Score> page, String number);
}
