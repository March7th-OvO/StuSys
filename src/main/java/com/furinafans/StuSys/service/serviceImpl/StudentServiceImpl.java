package com.furinafans.stusys.service.serviceImpl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.entity.Student;
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
        Page<Student> page = new Page<>(pageNum, pageSize);
        return studentMapper.selectPage(page, null);
    }
}
