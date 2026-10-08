package com.furinafans.stusys.common.base;

import java.io.Serializable;

import com.baomidou.mybatisplus.spring.service.IService;

public interface BaseService<T> extends IService<T> {
    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    T get(Serializable id);

    /**
     * 创建实体
     * @param entity
     * @return
     */
    T create(T entity);

    /**
     * 更新实体(先查询再修改)
     * @param entity
     * @return
     */
    T update(T entity);

    /**
     * 根据ID删除实体
     * @param id
     */
    void delete(Serializable id);
}
