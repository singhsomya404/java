package com.attendance.repository;

import com.attendance.model.Attendance;
import com.attendance.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentIdAndCourseIdAndAttendanceDate(Long studentId, Long courseId, LocalDate attendanceDate);

    List<Attendance> findByCourseIdAndAttendanceDate(Long courseId, LocalDate attendanceDate);

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    List<Attendance> findByStudentIdAndCourseIdOrderByAttendanceDateDesc(Long studentId, Long courseId);

    List<Attendance> findByCourseIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long courseId, LocalDate start, LocalDate end);

    List<Attendance> findByAttendanceDateBetweenOrderByAttendanceDateAsc(LocalDate start, LocalDate end);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, AttendanceStatus status);

    @Query("SELECT COUNT(DISTINCT a.attendanceDate) FROM Attendance a WHERE a.course.id = :courseId")
    long countDistinctDatesByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT a FROM Attendance a WHERE " +
           "(:courseId IS NULL OR a.course.id = :courseId) AND " +
           "(:studentId IS NULL OR a.student.id = :studentId) AND " +
           "(:startDate IS NULL OR a.attendanceDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.attendanceDate <= :endDate) " +
           "ORDER BY a.attendanceDate DESC, a.student.rollNumber ASC")
    List<Attendance> filterAttendance(
        @Param("courseId") Long courseId,
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
