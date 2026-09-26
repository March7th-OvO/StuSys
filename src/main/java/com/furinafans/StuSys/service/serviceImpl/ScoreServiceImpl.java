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
    // 注入mapper
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;

    @Override
    public Score addScore(Score score) {
        // 校验传入值是否合法
        if (score == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "新增Score不能为空");

        // // 校验插入值是否已经存在
        // if (scoreMapper.exists(new LambdaQueryWrapper<Score>()
        //         .eq(Score::getStudentId, score.getStudentId())
        //         .eq(Score::getCourseId, score.getCourseId())
        //         .eq(Score::getAcademicYear, score.getAcademicYear())
        //         .eq(Score::getTerm, score.getTerm())
        //         .eq(Score::getExamType, score.getExamType()))) {
        //     throw new BizException(ErrorCode.CONFLICT, "此成绩已经存在！");
        // }

        // 校验唯一性
        if (scoreMapper.insert(score) == 0)
            throw new BizException(ErrorCode.CONFLICT, "此Score已存在");

        return score;
    }

    @Override
    public boolean delScore(Long id) {
        // 校验ID是否合法
        if (id == null || id < 1)
            throw new BizException(ErrorCode.PARAM_ERROR, "ID不能为空或小于1！");

        // 判断删除是否成功
        if (scoreMapper.deleteById(id) > 0)
            return true;
        else
            throw new BizException(ErrorCode.NOT_FOUND, "此ID不存在！");
    }

    @Override
    public Score updateScore(Long id, Score score) {
        // 校验ID是否合法
        if (id == null || id < 1)
            throw new BizException(ErrorCode.PARAM_ERROR, "ID不能为空或小于1！");

        // 校验更新值是否为空
        if (score == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "更新Score不能为空");

        // 校验ID是否存在
        if (scoreMapper.update(score, new LambdaQueryWrapper<Score>()
                .eq(Score::getId, id)) == 0)
            throw new BizException(ErrorCode.NOT_FOUND, "此ID不存在！");

        return score;
    }

    @Override
    public IPage<Score> selectScoreByStuNum(Page<Score> page, String number) {
        // 校验Number是否合法
        if (number == null || number.isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "查询的Number不能为空！");

        // 校验Number是否存在
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .select(Student::getId)
                .eq(Student::getNumber, number));
        if (student == null)
            throw new BizException(ErrorCode.NOT_FOUND, "未查询到指定Student");

        return scoreMapper.selectPage(page, new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, student.getId()));
    }
}
