package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Grade;
import com.furinafans.stusys.mapper.GradeMapper;
import com.furinafans.stusys.service.GradeService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class GradeServiceImpl implements GradeService{
    private final GradeMapper gradeMapper;

    @Override 
    public Grade addGrade(Grade grade){
        gradeMapper.insert(grade);;
        return grade;
    }
    
    @Override 
    public void delGrade(Long id){
        gradeMapper.deleteById(id);
    }
    
    @Override 
    public Grade updateGrade(Grade grade){
        gradeMapper.updateById(grade);
        return this.selectGrade(grade.getId());
    }
    
    @Override 
    public Grade selectGrade(Long id){
        return gradeMapper.selectById(id);
    }
    
    @Override 
    public IPage<Grade> page(BasePageQuery<Grade> request){
        return gradeMapper.selectPage(request.toPage(), null);
    }
}
