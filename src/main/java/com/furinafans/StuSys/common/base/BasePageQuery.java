package com.furinafans.stusys.common.base;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data 
public class BasePageQuery {
    @Max (100) 
    @Min (1)
    private Long pageNum = 1L;
    @Max (100) 
    @Min (1)
    private Long pageSize = 10L;
}
