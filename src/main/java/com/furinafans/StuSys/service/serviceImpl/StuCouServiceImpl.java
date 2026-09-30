package com.furinafans.stusys.service.serviceImpl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.dto.StuCouPageDto;
import com.furinafans.stusys.dto.StuCouXuanDto;
import com.furinafans.stusys.entity.StuCou;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.StuCouMapper;
import com.furinafans.stusys.service.StuCouService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StuCouServiceImpl implements StuCouService {

    private final StuCouMapper scMapper;

    @Override
    public StuCou xuanKe(StuCouXuanDto sc) {
        StuCou sce = sc.toEntity();
        sce.setStatus(1);
        try {
            if (scMapper.insert(sce) != 1)
                throw new BizException(ErrorCode.SERVER_ERROR, "选课失败！");
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.CONFLICT, "同一学年学期的同一课程不能重读选择！");
        }
        return sce;
    }

    @Override
    public void tuiKe(Long id) {
        LambdaUpdateWrapper<StuCou> scW = new LambdaUpdateWrapper<>();
        scW
                .eq(StuCou::getId, id)
                .eq(StuCou::getStatus, 1)
                .set(StuCou::getStatus, 3);

        if (scMapper.update(scW) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "该选课记录不存在或当前状态禁止退课，退课失败！");
        }
    }

    @Override
    public void jieKe(Long id) {
        LambdaUpdateWrapper<StuCou> scW = new LambdaUpdateWrapper<>();
        scW
                .eq(StuCou::getId, id)
                .eq(StuCou::getStatus, 1)
                .set(StuCou::getStatus, 2);

        if (scMapper.update(scW) == 0) {
            throw new BizException(ErrorCode.CONFLICT, "该选课记录不存在或当前状态禁止结课，结课失败！");
        }
    }

    @Override
    public IPage<StuCou> page(StuCouPageDto request) {
        LambdaQueryWrapper<StuCou> pageW = new LambdaQueryWrapper<>();
        pageW
                .eq(request.getStudentId() != null, StuCou::getStudentId, request.getStudentId())
                .eq(request.getCourseId() != null, StuCou::getCourseId, request.getCourseId())
                .eq(StringUtils.isNotBlank(request.getAcademicYear()), StuCou::getAcademicYear,request.getAcademicYear())
                .eq(request.getTerm() != null, StuCou::getTerm, request.getTerm())
                .eq(request.getStatus() != null, StuCou::getStatus, request.getStatus())
                .orderByDesc(StuCou::getId);
        
        return scMapper.selectPage(request.toPage(), pageW);
    }

}
