package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.furinafans.stusys.mapper.StudentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.entity.Score;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.ScoreMapper;
import com.furinafans.stusys.service.ScoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;

    @Override
    public boolean addScore(Score score) {
        
        return scoreMapper.insertOrUpdate(score);
    }

    @Override
    public boolean delScore(Long id) {
        return scoreMapper.deleteById(id) > 0;
    }

    @Override
    public void updateScore(Long id, Score score) {
        LambdaQueryWrapper<Score> w = new LambdaQueryWrapper<>();
        w.eq(Score::getId, id);
        scoreMapper.update(score, w);
    }

    @Override
    public IPage<Score> selectScoreByStuNum(Page<Score> page, String number) {
        if (number == null || number.isBlank()) throw new BizException(ErrorCode.PARAM_ERROR, "查询的Number不能为空！");
        
        LambdaQueryWrapper<Student> stuW = new LambdaQueryWrapper<>();
        stuW.select(Student::getId).eq(Student::getNumber, number);
        Student student = studentMapper.selectOne(stuW);

        if (student == null) throw new BizException(ErrorCode.NOT_FOUND, "未查询到指定Student");

        LambdaQueryWrapper<Score> scoreW = new LambdaQueryWrapper<>();
        scoreW.eq(Score::getStudentId, student.getId());
        return scoreMapper.selectPage(page, scoreW);
    }
}
