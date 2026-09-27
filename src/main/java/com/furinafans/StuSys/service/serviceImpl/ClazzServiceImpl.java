package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.dto.ClazzPageDTO;
import com.furinafans.stusys.entity.Clazz;
import com.furinafans.stusys.mapper.ClazzMapper;
import com.furinafans.stusys.service.ClazzService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClazzServiceImpl implements ClazzService {
    private final ClazzMapper clazzMapper;

    @Override 
    public Clazz addClazz(Clazz clazz) {
        clazzMapper.insert(clazz);
        return clazz;
    }

    
    public void delClazz(Long id) {
        clazzMapper.deleteById(id);
    }

    public Clazz updateClazz(Clazz clazz) {
        clazzMapper.update(clazz, new LambdaQueryWrapper<Clazz>().eq(Clazz::getId, clazz.getId()));
        return clazz;
    }

    public Clazz selectClazz(Long id) {
        return clazzMapper.selectById(id);
    }

    public IPage<Clazz> pageClazz(ClazzPageDTO pageDTO) {
        LambdaQueryWrapper<Clazz> w = new LambdaQueryWrapper<>();
        w
                .like(StringUtils.isNotBlank(pageDTO.getName()), Clazz::getName, pageDTO.getName())
                .eq(pageDTO.getGradeId() != null, Clazz::getGradeId, pageDTO.getGradeId());
        return clazzMapper.selectPage(new Page<Clazz>(pageDTO.getPageNum(), pageDTO.getPageSize()), w);
    }
}
