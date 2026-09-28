package com.furinafans.stusys.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.dto.StudentPageDTO;
import com.furinafans.stusys.entity.Clazz;
import com.furinafans.stusys.entity.Student;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.ClazzMapper;
import com.furinafans.stusys.mapper.StudentMapper;
import com.furinafans.stusys.service.StudentService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    // 注入mapper
    private final StudentMapper studentMapper;
    private final ClazzMapper clazzMapper;

    // 插入新学生
    @Override
    public Student addStu(Student stu) {
        // 校验传入值是否合法
        if (stu == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "新增学生不能为null");
        if (stu.getName() == null || stu.getName().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "学生姓名不能为null或空格");
        if (stu.getNumber() == null || stu.getNumber().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "学生学号不能为null或空格");
        if (stu.getClazzId() == null || stu.getClazzId() <= 0)
            throw new BizException(ErrorCode.PARAM_ERROR, "班级Id不能小于等于0");

        // 校验唯一性
        if (studentMapper.insert(stu) == 0)
            throw new BizException(ErrorCode.CONFLICT, "此Student已存在");

        return stu;
    }

    // 根据学号Number查询学生
    @Override
    public Student searchStuByNum(String num) {
        Student stu = studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getNumber, num));
        if (stu == null)
            throw new BizException(ErrorCode.NOT_FOUND, "此学生不存在！");
        return stu;
    }

    // 分页查询学生
    @Override
    public IPage<Student> page(StudentPageDTO pageDTO) {
        LambdaQueryWrapper<Student> studentW = new LambdaQueryWrapper<>();
        // 组装Student的查询where语句
        studentW
                .like(
                        StringUtils.isNotBlank(pageDTO.getName()),
                        Student::getName,
                        pageDTO.getName())
                .eq(
                        StringUtils.isNotBlank(pageDTO.getNumber()),
                        Student::getNumber,
                        pageDTO.getNumber())
                .eq(
                        pageDTO.getClassId() != null,
                        Student::getClazzId,
                        pageDTO.getClassId());

        // 1.校验GradeId是否存在
        if (pageDTO.getGradeId() != null) {
            LambdaQueryWrapper<Clazz> clazzW = new LambdaQueryWrapper<>();
            clazzW
                    .select(Clazz::getId)
                    .eq(Clazz::getGradeId, pageDTO.getGradeId());
            List<Clazz> clazzList = clazzMapper.selectList(clazzW);

            // 2.如果在Class表中查询不到Grade的id，说明不存在该Grade的学生，直接return空的Page
            if (clazzList.isEmpty()) {
                return pageDTO.toPage();
            }

            // 3.如果能查到Grade对应的Class，就把这些Class放入一个List中，然后在将其中的id提取出来做成一个新的List
            List<Long> ids = clazzList.stream().map(Clazz::getId).toList();

            // 4.将ids中的classId作为Student表的查询条件
            studentW.in(Student::getClazzId, ids);
        }
        studentW.orderByAsc(Student::getId);

        // 5.studentMapper的selectPage会将查询到的结果回填给Page
        return studentMapper.selectPage(pageDTO.toPage(), studentW);
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
    public Student updateStu(String num, Student stu) {
        if (num == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "输入学号不能为null");
        if (stu == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生不能为null");
        if (stu.getName() == null || stu.getName().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生姓名不能为null或空格");
        if (stu.getNumber() == null || stu.getNumber().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后学生学号不能为null或空格");
        if (stu.getClazzId() == null || stu.getClazzId() <= 0)
            throw new BizException(ErrorCode.PARAM_ERROR, "修改后班级Id不能小于等于0");

        // 使用LambdaQueryWrapper查询是否有相同Number存在
        // 查冲突：用ne排除除了自己，还有谁占用了新学号
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getNumber, stu.getNumber())
                .ne(Student::getNumber, num)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "修改失败，新学号已存在！");
        }

        // 用旧学号定位要改的那条
        int n = studentMapper.update(stu, new LambdaUpdateWrapper<Student>().eq(Student::getNumber, num));
        if (n < 1)
            throw new BizException(ErrorCode.NOT_FOUND, "修改失败，你输入的学号不存在！");

        return stu;
    }
}
