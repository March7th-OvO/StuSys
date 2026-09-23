package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.entity.Course;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.CourseMapper;
import com.furinafans.stusys.service.CourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;

    @Override
    public Integer addCourse(Course course) {
        if (course == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新增Course不能为null");
        }

        if (course.getName() == null || course.getName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新增CourseName不能为null");
        }

        if (course.getCode() == null || course.getCode().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新增CourseCode不能为null");
        }

        // 去除Name和Code中的空格
        course.setName(course.getName().strip());
        course.setCode(course.getCode().strip());

        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        w.eq(Course::getName, course.getName())
                .or()
                .eq(Course::getCode, course.getCode());
        Long r = courseMapper.selectCount(w);
        if (r > 0) {
            throw new BizException(ErrorCode.CONFLICT, "当前Course的Name或Code重复");
        }

        Integer n = courseMapper.insert(course);
        if (n < 1) {
            throw new BizException(ErrorCode.SERVER_ERROR, "数据库插入Course失败");
        }
        return course.getId();
    }

    @Override
    public void delCourse(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "name不能为空");
        }

        // 去除name中的空格
        name = name.strip();

        int n = courseMapper.delete(new LambdaQueryWrapper<Course>().eq(Course::getName, name));
        if (n < 1) {
            throw new BizException(ErrorCode.NOT_FOUND, name + "删除失败！课程不存在");
        }
    }

    @Override
    public void updateCourse(String name, Course course) {
        if (name == null || name.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "Name不能为空");
        }

        if (course == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "Course不能为空");
        }

        if (course.getCode() == null || course.getCode().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新Code不能为空");
        }

        if (course.getName() == null || course.getName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "新Name不能为空");
        }

        // 清除请求体的id、去除Name和Code的空格
        course.setId(null);
        course.setName(course.getName().strip());
        course.setCode(course.getCode().strip());

        if (courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getName, course.getName())
                .ne(Course::getName, name)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, course.getName() + "不可用！已经存在");
        }

        int update = courseMapper.update(course, new LambdaUpdateWrapper<Course>()
                .eq(Course::getName, name));
        if (update < 1) {
            throw new BizException(ErrorCode.NOT_FOUND, name + "未找到，修改失败！");
        }
    }

    @Override
    public Course searchCourseByCode(String code) {
        return courseMapper.selectOne(new LambdaQueryWrapper<Course>().eq(Course::getCode, code));
    }
}
