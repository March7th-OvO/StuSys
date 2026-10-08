package com.furinafans.stusys.common.base;

import java.io.Serializable;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.exception.BizException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T>
        implements BaseService<T> {

    @Override
    public T get(Serializable id) {
        T entity = getById(id);
        return entity;
    }

    @Override
    public T create(T entity) {
        int count = baseMapper.insert(entity);
        boolean result = count > 0;
        return entity;
    }

    @Override
    public T update(T entity) {
        Serializable id = getEntityId(entity);
        if (id == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "未获取到主键ID");

        T dbEntity = getById(id);
        if (dbEntity == null)
            throw new BizException(ErrorCode.NOT_FOUND, "数据库中未查询到记录");

        boolean result = updateById(entity);
        return entity;
    }

    @Override
    public void delete(Serializable id) {
        T entity = getById(id);
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "未找到此实体");
        boolean result = removeById(entity);
    }

    protected Serializable getEntityId(T entity) {
        // 第一步，拿到当前传入实体的TableInfo
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityClass());

        // 如果拿不到，说明这个类没有被MyBatis-Plus管理，直接return null
        if (tableInfo == null)
            return null;

        String idName = tableInfo.getKeyProperty();
            if (idName == null)
                throw new BizException(ErrorCode.PARAM_ERROR, "主键不存在");
        try {
            Serializable idValue = (Serializable) tableInfo.getPropertyValue(entity, idName);
            return idValue;
        } catch (Exception e) {
            log.warn("获取实体ID失败", e);
            return null;
        }
    }

}
