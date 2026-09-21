CREATE DATABASE IF NOT EXISTS stu_sys
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;
USE stu_sys;

CREATE TABLE IF NOT EXISTS grades (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) NOT NULL COMMENT '年级/届数',
    CONSTRAINT pk_grades PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '年级表';

CREATE TABLE IF NOT EXISTS classes (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) NOT NULL COMMENT '班级名',
    grade_id BIGINT UNSIGNED NOT NULL COMMENT '年级ID',
    CONSTRAINT pk_classes PRIMARY KEY (id),
    CONSTRAINT fk_grades_classes FOREIGN KEY (grade_id) REFERENCES grades(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '班级表';

CREATE TABLE IF NOT EXISTS students (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) NOT NULL COMMENT '姓名',
    number VARCHAR(20) NOT NULL COMMENT '学号',
    class_id BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    CONSTRAINT pk_students PRIMARY KEY (id),
    CONSTRAINT fk_students_classes FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT uk_number UNIQUE (number);
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生表';

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) NOT NULL COMMENT '课程名',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '课程代码',
    CONSTRAINT pk_courses PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程表';

CREATE TABLE IF NOT EXISTS scores (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    score DECIMAL(6,2) COMMENT '成绩',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    term VARCHAR(20) NOT NULL COMMENT '学期',
    exam_type VARCHAR(20) NOT NULL COMMENT '考试类型',
    CONSTRAINT pk_scores PRIMARY KEY (id),
    CONSTRAINT fk_scores_students FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_scores_courses FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uk_scores_term UNIQUE (student_id, course_id, term, exam_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '成绩表';

CREATE TABLE IF NOT EXISTS student_course (
    id BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    term VARCHAR(20) NOT NULL COMMENT '学期',
    status ENUM('已选完', '已修完', '已退选') NOT NULL COMMENT '课程状态',
    CONSTRAINT pk_students_courses PRIMARY KEY (id),
    CONSTRAINT fk_courses FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_stu_cour_course FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT uk_stu_cour_term UNIQUE (student_id, course_id, term)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '学生_课程表';