package com.attendance.service;

import com.attendance.dto.StudentDto;
import com.attendance.model.Course;
import com.attendance.model.Role;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.repository.CourseRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentDto> getAllStudents() {
        return studentRepository.findAllOrderByRollNumber().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Student getStudentEntityById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
    }

    public StudentDto getStudentById(Long id) {
        return toDto(getStudentEntityById(id));
    }

    public StudentDto getStudentByRollNumber(String rollNumber) {
        Student student = studentRepository.findByRollNumber(rollNumber)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with Roll Number: " + rollNumber));
        return toDto(student);
    }

    public List<StudentDto> getStudentsByCourse(Long courseId) {
        return studentRepository.findByCourseId(courseId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<StudentDto> filterStudents(String department, Integer semester) {
        if (department != null && !department.trim().isEmpty() && semester != null) {
            return studentRepository.findByDepartmentAndSemester(department.trim(), semester).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        }
        return getAllStudents();
    }

    @Transactional
    public StudentDto addStudent(StudentDto dto) {
        if (dto.getRollNumber() == null || dto.getRollNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number is required.");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        String rollNumber = dto.getRollNumber().trim().toUpperCase();
        String email = dto.getEmail().trim().toLowerCase();

        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new IllegalArgumentException("A student with roll number '" + rollNumber + "' already exists.");
        }
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A student with email '" + email + "' already exists.");
        }

        // 1. Create or link user account for login
        String password = (dto.getPassword() != null && !dto.getPassword().trim().isEmpty())
            ? dto.getPassword().trim()
            : "student123";

        User user = new User(rollNumber, password, Role.STUDENT, dto.getFullName().trim());
        user = userRepository.save(user);

        // 2. Create student entity
        Student student = new Student();
        student.setRollNumber(rollNumber);
        student.setFullName(dto.getFullName().trim());
        student.setEmail(email);
        student.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : "");
        student.setDepartment(dto.getDepartment() != null ? dto.getDepartment().trim() : "Computer Science");
        student.setSemester(dto.getSemester() != null ? dto.getSemester() : 1);
        student.setSection(dto.getSection() != null ? dto.getSection().trim().toUpperCase() : "A");
        student.setUser(user);

        // 3. Enroll courses
        Set<Course> coursesToEnroll = new HashSet<>();
        if (dto.getEnrolledCourseIds() != null && !dto.getEnrolledCourseIds().isEmpty()) {
            coursesToEnroll.addAll(courseRepository.findAllById(dto.getEnrolledCourseIds()));
        } else {
            // Auto-enroll in matching department & semester courses, or all current courses if none match
            List<Course> matchingCourses = courseRepository.findByDepartmentAndSemester(student.getDepartment(), student.getSemester());
            if (matchingCourses.isEmpty()) {
                matchingCourses = courseRepository.findAll();
            }
            coursesToEnroll.addAll(matchingCourses);
        }
        student.setEnrolledCourses(coursesToEnroll);

        Student saved = studentRepository.save(student);
        return toDto(saved);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = getStudentEntityById(id);

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            student.setFullName(dto.getFullName().trim());
            if (student.getUser() != null) {
                student.getUser().setFullName(student.getFullName());
            }
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(student.getEmail()) && studentRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email '" + newEmail + "' is already registered to another student.");
            }
            student.setEmail(newEmail);
        }
        if (dto.getPhone() != null) {
            student.setPhone(dto.getPhone().trim());
        }
        if (dto.getDepartment() != null && !dto.getDepartment().trim().isEmpty()) {
            student.setDepartment(dto.getDepartment().trim());
        }
        if (dto.getSemester() != null) {
            student.setSemester(dto.getSemester());
        }
        if (dto.getSection() != null && !dto.getSection().trim().isEmpty()) {
            student.setSection(dto.getSection().trim().toUpperCase());
        }
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty() && student.getUser() != null) {
            student.getUser().setPassword(dto.getPassword().trim());
        }
        if (dto.getEnrolledCourseIds() != null) {
            Set<Course> courses = new HashSet<>(courseRepository.findAllById(dto.getEnrolledCourseIds()));
            student.setEnrolledCourses(courses);
        }

        Student updated = studentRepository.save(student);
        return toDto(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentEntityById(id);
        User user = student.getUser();
        studentRepository.delete(student);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public void enrollCourse(Long studentId, Long courseId) {
        Student student = getStudentEntityById(studentId);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        student.getEnrolledCourses().add(course);
        studentRepository.save(student);
    }

    public StudentDto toDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setRollNumber(student.getRollNumber());
        dto.setFullName(student.getFullName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setDepartment(student.getDepartment());
        dto.setSemester(student.getSemester());
        dto.setSection(student.getSection());
        if (student.getUser() != null) {
            dto.setUserId(student.getUser().getId());
        }
        if (student.getEnrolledCourses() != null) {
            dto.setEnrolledCourseIds(
                student.getEnrolledCourses().stream().map(Course::getId).collect(Collectors.toSet())
            );
        }
        return dto;
    }
}
