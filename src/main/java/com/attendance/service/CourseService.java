package com.attendance.service;

import com.attendance.dto.CourseDto;
import com.attendance.model.Course;
import com.attendance.repository.CourseRepository;
import com.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findAllByOrderByCourseCodeAsc().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));
    }

    public CourseDto getCourseById(Long id) {
        return toDto(getCourseEntityById(id));
    }

    @Transactional
    public CourseDto addCourse(CourseDto dto) {
        if (dto.getCourseCode() == null || dto.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required.");
        }
        if (dto.getCourseName() == null || dto.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required.");
        }
        if (courseRepository.existsByCourseCode(dto.getCourseCode().trim())) {
            throw new IllegalArgumentException("Course with code '" + dto.getCourseCode() + "' already exists.");
        }

        Course course = new Course(
            dto.getCourseCode().trim().toUpperCase(),
            dto.getCourseName().trim(),
            dto.getDepartment() != null ? dto.getDepartment().trim() : "General",
            dto.getSemester() != null ? dto.getSemester() : 1
        );

        Course saved = courseRepository.save(course);
        return toDto(saved);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    public CourseDto toDto(Course course) {
        int studentCount = studentRepository.findByCourseId(course.getId()).size();
        return new CourseDto(
            course.getId(),
            course.getCourseCode(),
            course.getCourseName(),
            course.getDepartment(),
            course.getSemester(),
            studentCount
        );
    }
}
