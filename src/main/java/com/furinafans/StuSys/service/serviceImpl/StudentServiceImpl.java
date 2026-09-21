package com.furinafans.stusys.service.serviceImpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.Result;
import com.furinafans.stusys.common.enums.ErrorCode;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.StudentMapper;
import com.furinafans.stusys.service.StudentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor 
public class StudentServiceImpl implements StudentService {
    
    private final StudentMapper studentMapper;
    
    @Override
    public Integer addStudent(Student stu) {
        if (stu == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新增学生不能为null");
        }

        if (stu.getName() == null || stu.getName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "学生姓名不能为null或空格");
        }

        if (stu.getNumber() == null || stu.getNumber().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "学生学号不能为null或空格");
        }

        if (stu.getClassId() <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "班级Id不能小于等于0");
        }

        String name = stu.getName().strip();
        stu.setName(name);

        Integer result = studentMapper.insert(stu);
        if (result == 0) {
            throw new BizException(ErrorCode.SERVER_ERROR, "新增学生失败！");
        }
        return stu.getId();
    }
    
    @Override
    public Student getStudentById(Integer id) {
        Student stu = studentMapper.selectById(id);
        if (stu == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "该学生不存在");
        }
        return stu;
    }

    @Override 
    public IPage<Student> page(Integer pageNum, Integer pageSize){
        if (pageSize <= 0 || pageSize > 200) {
            throw new BizException(ErrorCode.PARAM_ERROR, "pageSize必须在 1~200 之间");
        }

        if (pageNum <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "pageNum不能小于等于0");
        }
        
        Page<Student> page = new Page<>(pageNum, pageSize);
        return studentMapper.selectPage(page, null);
    }
}
