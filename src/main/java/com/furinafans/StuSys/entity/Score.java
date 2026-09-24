package com.furinafans.stusys.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("Scores")
public class Score {
    @TableId(type = IdType.AUTO) 
    private Long id;
    private BigDecimal score;
    private Integer courseId;
    private Integer studentId;
    private String academicYear;
    private String term;
    private String examType;
}
