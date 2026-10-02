package com.attendance.repository;

import com.attendance.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUser_Username(String username);
    boolean existsByRollNumber(String rollNumber);
    boolean existsByEmail(String email);

    List<Student> findByDepartmentAndSemester(String department, Integer semester);

    @Query("SELECT s FROM Student s JOIN s.enrolledCourses c WHERE c.id = :courseId ORDER BY s.rollNumber ASC")
    List<Student> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT s FROM Student s ORDER BY s.rollNumber ASC")
    List<Student> findAllOrderByRollNumber();
}
