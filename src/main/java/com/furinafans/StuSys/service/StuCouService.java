package com.furinafans.stusys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.dto.StuCouXuanDto;
import com.furinafans.stusys.entity.StuCou;
import com.furinafans.stusys.vo.StuCouVo;

public interface StuCouService {
    StuCou xuanKe(StuCouXuanDto sc);
    void tuiKe(Long id);
    void jieKe(Long id);
    IPage<StuCou> page(StuCouVo request);
}
