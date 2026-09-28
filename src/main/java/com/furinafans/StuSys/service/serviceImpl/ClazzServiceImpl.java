package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.dto.ClazzPageDTO;
import com.furinafans.stusys.entity.Clazz;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.ClazzMapper;
import com.furinafans.stusys.service.ClazzService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClazzServiceImpl implements ClazzService {
    private final ClazzMapper clazzMapper;

    @Override 
    public Clazz addClazz(Clazz clazz) {
        if (clazz == null) throw new BizException(ErrorCode.PARAM_ERROR, "新增Class不能为空！");
        if (clazzMapper.insert(clazz) == 0) throw new BizException(ErrorCode.DATA_ERROR, "Class新增失败！");
        return clazz;
    }

    @Override 
    public void delClazz(Long id) {
        if (id == null) throw new BizException(ErrorCode.PARAM_ERROR, "班级ID不能为空！");
        if (clazzMapper.deleteById(id) == 0) throw new BizException(ErrorCode.NOT_FOUND, "删除失败！此Class不存在！");
    }

    @Override 
    public Clazz updateClazz(Clazz clazz) {
        if (clazz == null) throw new BizException(ErrorCode.PARAM_ERROR, "新的Class不能为空！");
        if (clazz.getId() == null) throw new BizException(ErrorCode.NOT_FOUND, "Class的ID不能为空！");
        if (clazzMapper.updateById(clazz) == 0) throw new BizException(ErrorCode.NOT_FOUND, "修改失败！此Class不存在！");
        return clazz;
    }

    @Override 
    public Clazz selectClazz(Long id) {
        Clazz clazz = clazzMapper.selectById(id);
        if (clazz == null) throw new BizException(ErrorCode.NOT_FOUND, "此Class不存在！");
        return clazz;
    }

    @Override 
    public IPage<Clazz> pageClazz(ClazzPageDTO pageDTO) {
        LambdaQueryWrapper<Clazz> w = new LambdaQueryWrapper<>();
        Clazz clazz = pageDTO.toEntity();
        w
                .like(StringUtils.isNotBlank(clazz.getName()), Clazz::getName, clazz.getName())
                .eq(clazz.getGradeId() != null, Clazz::getGradeId, clazz.getGradeId());
        return clazzMapper.selectPage(pageDTO.toPage(), w);
    }
}
