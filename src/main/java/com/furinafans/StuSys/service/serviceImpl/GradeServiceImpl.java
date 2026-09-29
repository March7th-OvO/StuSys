package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.entity.Grade;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.GradeMapper;
import com.furinafans.stusys.service.GradeService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class GradeServiceImpl implements GradeService{
    private final GradeMapper gradeMapper;

    @Override 
    public Grade addGrade(Grade grade){
        if (grade == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "新增Grade不能为空！");
        gradeMapper.insert(grade);;
        return grade;
    }
    
    @Override 
    public void delGrade(Long id){
        int r = gradeMapper.deleteById(id);
        if (r == 0)
            throw new BizException(ErrorCode.NOT_FOUND, "此Grade不存在！");
    }
    
    @Override 
    public Grade updateGrade(Grade grade){
        int r = gradeMapper.updateById(grade);
        if (r == 0)
            throw new BizException(ErrorCode.NOT_FOUND, "此ID不存在！");
        return this.selectGrade(grade.getId());
    }
    
    @Override 
    public Grade selectGrade(Long id){
        Grade grade = gradeMapper.selectById(id);
        if (grade == null)
            throw new BizException(ErrorCode.NOT_FOUND, "此ID不存在！");
        return grade;
    }
    
    @Override 
    public IPage<Grade> page(BasePageQuery<Grade> request){
        return gradeMapper.selectPage(request.toPage(), null);
    }
}
