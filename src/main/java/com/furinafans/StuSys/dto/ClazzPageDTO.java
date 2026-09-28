package com.furinafans.stusys.dto;

import com.furinafans.stusys.common.base.BasePageQuery;
import com.furinafans.stusys.entity.Clazz;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClazzPageDTO extends BasePageQuery {
    @Size(max = 20, message = "Name长度不能超过20个字符")
    private String name;

    private Long gradeId;

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
    }

    public Clazz toEntity() {
        return Clazz.builder()
                .name(this.name)
                .gradeId(this.gradeId)
                .build();
    }
}
