package com.attendance.controller;

import com.attendance.dto.StudentDto;
import com.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer semester) {
        if (department != null || semester != null) {
            return ResponseEntity.ok(studentService.filterStudents(department, semester));
        }
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/roll/{rollNumber}")
    public ResponseEntity<StudentDto> getStudentByRollNumber(@PathVariable String rollNumber) {
        return ResponseEntity.ok(studentService.getStudentByRollNumber(rollNumber));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudentDto>> getStudentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.getStudentsByCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody StudentDto dto) {
        try {
            StudentDto created = studentService.addStudent(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody StudentDto dto) {
        try {
            StudentDto updated = studentService.updateStudent(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
