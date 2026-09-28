package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.dto.ClazzPageDTO;
import com.furinafans.stusys.entity.Clazz;

public interface ClazzService {
    Clazz addClazz(Clazz clazz);
    
    void delClazz(Long id);
    
    Clazz updateClazz(Clazz clazz);
    
    Clazz selectClazz(Long id);
    
    IPage<Clazz> pageClazz(ClazzPageDTO pageDTO);
}
