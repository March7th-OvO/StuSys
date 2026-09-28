package com.furinafans.stusys.common.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data 
public class BasePageQuery<T> {
    // 分页通常只限制每页的条数，而页码只限制大于0
    @Min (1)
    private Long pageNum = 1L;
    @Max (100) 
    @Min (1)
    private Long pageSize = 10L;

    public Page<T> toPage(){
        return new Page<>(this.pageNum, this.pageSize);
    }
}
