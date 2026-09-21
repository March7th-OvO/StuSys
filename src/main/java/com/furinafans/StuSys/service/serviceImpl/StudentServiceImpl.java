package com.furinafans.stusys.service.serviceImpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.Result;
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
    public Integer addStudent(Student student) {
        studentMapper.insert(student);
        return student.getId();
    }
    
    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.selectById(id);
    }

    @Override 
    public IPage<Student> page(Integer pageNum, Integer pageSize){
        if (pageSize <= 0 || pageSize > 200) {
            throw new BizException(Result.PARAM_ERROR, "pageSize必须在 1~200 之间");
        }

        if (pageNum <= 0) {
            throw new BizException(Result.PARAM_ERROR, "pageNum不能小于等于0");
        }
        
        Page<Student> page = new Page<>(pageNum, pageSize);
        return studentMapper.selectPage(page, null);
    }
}
