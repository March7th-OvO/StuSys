package com.furinafans.stusys.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.constant.ErrorCode;
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

    // 插入新学生
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

        if (stu.getClassId() == null || stu.getClassId() <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "班级Id不能小于等于0");
        }

        // 去除Name和Number的多余空格
        String name = stu.getName().strip();
        stu.setName(name);
        String num = stu.getNumber().strip();
        stu.setNumber(num);

        // 使用LambdaQueryWrapper查询是否有相同Number存在
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getNumber, num)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "新增失败，此学号已存在！");
        }

        // 插入失败，修改0行校验
        if (studentMapper.insert(stu) == 0) {
            throw new BizException(ErrorCode.SERVER_ERROR, "新增学生失败！");
        }
        return stu.getId();
    }

    // 根据学号Number查询学生
    @Override
    public Student searchStuByNum(String num) {
        Student stu = studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getNumber, num));
        if (stu == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "此学生不存在！");
        }
        return stu;
    }

    // 分页查询全部学生
    @Override
    public IPage<Student> page(Integer pageNum, Integer pageSize) {
        if (pageSize <= 0 || pageSize > 200) {
            throw new BizException(ErrorCode.PARAM_ERROR, "pageSize必须在 1~200 之间");
        }

        if (pageNum <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "pageNum不能小于等于0");
        }

        Page<Student> page = new Page<>(pageNum, pageSize);
        return studentMapper.selectPage(page, null);
    }

    // 根据学号(String)删除学生
    @Override
    public void delete(String num) {
        int n = studentMapper.delete(new LambdaQueryWrapper<Student>().eq(Student::getNumber, num));
        if (n < 1) {
            throw new BizException(ErrorCode.NOT_FOUND, "删除失败，该学生不存在！");
        }
    }

    // 根据Number学号修改Student
    @Override
    public void updateStudent(String num, Student stu) {
        if (num == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "输入学号不能为null");
        }
        if (stu == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生不能为null");
        }

        if (stu.getName() == null || stu.getName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生姓名不能为null或空格");
        }

        if (stu.getNumber() == null || stu.getNumber().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生学号不能为null或空格");
        }

        if (stu.getClassId() == null || stu.getClassId() <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后班级Id不能小于等于0");
        }

        // 去除number空格多余的空格
        stu.setNumber(stu.getNumber().strip());

        // 使用LambdaQueryWrapper查询是否有相同Number存在
        // 查冲突：用ne排除除了自己，还有谁占用了新学号
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getNumber, stu.getNumber())
                .ne(Student::getNumber, num)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "修改失败，新学号已存在！");
        }

        // 用旧学号定位要改的那条
        int n = studentMapper.update(stu, new LambdaUpdateWrapper<Student>().eq(Student::getNumber, num));
        if (n < 1) {
            throw new BizException(ErrorCode.NOT_FOUND, "修改失败，你输入的学号不存在！");
        }
    }
}
