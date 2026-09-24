package com.furinafans.stusys.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.entity.Score;

public interface ScoreService {
    boolean addScore(Score score);

    boolean delScore(Score score);

    void updateScore(Score score);

    IPage<Score> selectScoreByStuNum(Page<Score> page, String number);
}
