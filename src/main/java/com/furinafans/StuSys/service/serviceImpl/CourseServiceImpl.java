package com.furinafans.stusys.service.serviceImpl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.furinafans.stusys.common.constant.ErrorCode;
import com.furinafans.stusys.dto.CoursePageDTO;
import com.furinafans.stusys.entity.Course;
import com.furinafans.stusys.exception.BizException;
import com.furinafans.stusys.mapper.CourseMapper;
import com.furinafans.stusys.service.CourseService;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    // 注入mapper
    private final CourseMapper courseMapper;

    @Override
    public Course addCourse(Course course) {
        // 校验传入值是否合法
        if (course == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "新增Course不能为null");
        if (course.getName() == null || course.getName().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "新增CourseName不能为null");
        if (course.getCode() == null || course.getCode().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "新增CourseCode不能为null");

        // 校验唯一性
        if (courseMapper.insert(course) == 0)
            throw new BizException(ErrorCode.CONFLICT, "此Course已存在");
        return course;
    }

    @Override
    public void delCourse(Long id) {
        int n = courseMapper.deleteById(id);
        if (n < 1)
            throw new BizException(ErrorCode.NOT_FOUND, "删除失败！课程不存在");
    }

    @Override
    public Course updateCourse(String name, Course course) {
        if (name == null || name.isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "Name不能为空");
        if (course == null)
            throw new BizException(ErrorCode.PARAM_ERROR, "Course不能为空");
        if (course.getCode() == null || course.getCode().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "新Code不能为空");
        if (course.getName() == null || course.getName().isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "新Name不能为空");
        if (courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getName, course.getName())
                .ne(Course::getName, name)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, course.getName() + "不可用！已经存在");
        }

        int update = courseMapper.update(course, new LambdaUpdateWrapper<Course>()
                .eq(Course::getName, name));
        if (update < 1)
            throw new BizException(ErrorCode.NOT_FOUND, name + "未找到，修改失败！");

        return course;
    }

    @Override
    public Course searchCourseByCode(String code) {
        if (code == null || code.isBlank())
            throw new BizException(ErrorCode.PARAM_ERROR, "Code不能为空！");

        String codee = code.strip();

        Course result = courseMapper.selectOne(new LambdaQueryWrapper<Course>().eq(Course::getCode, codee));
        if (result == null)
            throw new BizException(ErrorCode.NOT_FOUND, "该Course不存在！");
        return result;
    }

    // 分页查询，支持根据Name模糊查询
    @Override
    public IPage<Course> page(CoursePageDTO pageDTO) {
        LambdaQueryWrapper<Course> courseW = new LambdaQueryWrapper<>();
        courseW
                .like(StringUtils.isNotBlank(pageDTO.getName()), Course::getName, pageDTO.getName());
        return courseMapper.selectPage(pageDTO.toPage(), courseW);
    }
}
