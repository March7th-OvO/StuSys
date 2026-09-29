package com.furinafans.stusys.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Grade;

public interface GradeService {
    
    Grade addGrade(Grade grade);
    
    void delGrade(Long id);
    
    Grade updateGrade(Grade grade);
    
    Grade selectGrade(Long id);
    
    IPage<Grade> page(BasePageQuery<Grade> request);
}