package com.furinafans.stusys.common.base;

import java.io.Serializable;

import org.springframework.web.bind.annotation.*;

import com.furinafans.stusys.common.Result;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseController<S extends BaseService<T>, T> {
    final protected S service;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取实体")
    public Result<T> get(@PathVariable("id") Serializable id) {
        T entity = service.get(id);
        return Result.success(entity);
    }

    @PostMapping
    @Operation(summary = "创建新实体")
    public Result<T> create(@Valid @RequestBody T entity) {
        T result = service.create(entity);
        return Result.success(result);
    }

    @PutMapping
    @Operation(summary = "更新实体")
    public Result<T> update(@Valid @RequestBody T entity) {
        T result = service.update(entity);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除实体")
    public Result<Void> delete(@PathVariable("id") Serializable id) {
        service.delete(id);
        return Result.success();
    }
}
