package com.furinafans.stusys.common.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public class MyBaseServiceImpl<T extends BaseMapper<T>> {
    public T update(T entity){
        return entity;
    }
}
