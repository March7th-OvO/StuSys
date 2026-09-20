package com.furinafans.stusys.service.serviceImpl;

import java.util.List;

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
    public List<Student> listAll() {
        return studentMapper.selectList(null);
    }
    
    @Override
    public Integer addStudent(Student student) {
        studentMapper.insert(student);
        return student.getId();
    }
    
    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.selectById(id);
    }
}
