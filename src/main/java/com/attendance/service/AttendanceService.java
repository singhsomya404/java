package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.model.*;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.CourseRepository;
import com.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public List<AttendanceRecordDto> markAttendance(MarkAttendanceRequest request) {
        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        if (request.getAttendanceDate() == null) {
            throw new IllegalArgumentException("Attendance date is required.");
        }
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new IllegalArgumentException("At least one student attendance entry is required.");
        }

        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + request.getCourseId()));

        LocalDate date = request.getAttendanceDate();
        List<Attendance> savedRecords = new ArrayList<>();

        for (MarkAttendanceRequest.StudentEntry entry : request.getEntries()) {
            if (entry.getStudentId() == null) continue;

            Student student = studentRepository.findById(entry.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + entry.getStudentId()));

            AttendanceStatus status = entry.getStatus() != null ? entry.getStatus() : AttendanceStatus.PRESENT;

            // Check if record already exists for student + course + date
            Optional<Attendance> existingOpt = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendanceDate(student.getId(), course.getId(), date);

            Attendance record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.setStatus(status);
                record.setRemarks(entry.getRemarks());
                record.setMarkedAt(LocalDateTime.now());
            } else {
                record = new Attendance(student, course, date, status, entry.getRemarks());
            }

            savedRecords.add(attendanceRepository.save(record));
        }

        return savedRecords.stream().map(this::toRecordDto).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAttendanceSheet(Long courseId, LocalDate date) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        List<Student> students = studentRepository.findByCourseId(courseId);
        if (students.isEmpty()) {
            // fallback to all students matching course semester or all students
            students = studentRepository.findAllOrderByRollNumber();
        }

        // Map existing attendance records
        List<Attendance> existing = attendanceRepository.findByCourseIdAndAttendanceDate(courseId, date);
        Map<Long, Attendance> recordMap = existing.stream()
            .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (e1, e2) -> e1));

        List<Map<String, Object>> sheet = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> item = new HashMap<>();
            item.put("studentId", s.getId());
            item.put("rollNumber", s.getRollNumber());
            item.put("studentName", s.getFullName());
            item.put("department", s.getDepartment());
            item.put("semester", s.getSemester());
            item.put("section", s.getSection());

            Attendance att = recordMap.get(s.getId());
            if (att != null) {
                item.put("status", att.getStatus().name());
                item.put("remarks", att.getRemarks() != null ? att.getRemarks() : "");
                item.put("recordId", att.getId());
                item.put("alreadyMarked", true);
            } else {
                item.put("status", "PRESENT"); // default selection for quick marking
                item.put("remarks", "");
                item.put("recordId", null);
                item.put("alreadyMarked", false);
            }
            sheet.add(item);
        }

        return sheet;
    }

    public AttendanceSummaryDto getStudentOverallAttendance(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        List<Attendance> allRecords = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);

        int total = allRecords.size();
        int present = 0;
        int absent = 0;
        int late = 0;
        int excused = 0;

        for (Attendance a : allRecords) {
            switch (a.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
                case EXCUSED -> excused++;
            }
        }

        // Attended is Present + Late + Excused
        int attended = present + late + excused;
        double percentage = total > 0 ? Math.round((attended * 100.0 / total) * 10.0) / 10.0 : 100.0;

        AttendanceSummaryDto summary = new AttendanceSummaryDto();
        summary.setStudentId(student.getId());
        summary.setRollNumber(student.getRollNumber());
        summary.setStudentName(student.getFullName());
        summary.setDepartment(student.getDepartment());
        summary.setSemester(student.getSemester());
        summary.setTotalClasses(total);
        summary.setAttendedClasses(attended);
        summary.setPresentClasses(present);
        summary.setAbsentClasses(absent);
        summary.setLateClasses(late);
        summary.setExcusedClasses(excused);
        summary.setAttendancePercentage(percentage);

        // Status and Shortage
        summary.setShortage(percentage < 75.0);
        if (percentage >= 75.0) {
            summary.setStatusBadge("GOOD");
        } else if (percentage >= 65.0) {
            summary.setStatusBadge("WARNING");
        } else {
            summary.setStatusBadge("CRITICAL");
        }

        // Target calculation
        if (percentage < 75.0 && total > 0) {
            // Needed consecutive classes to hit 75%:
            // (attended + x) / (total + x) >= 0.75 => x >= 3*total - 4*attended
            long needed = Math.max(0, (3L * total) - (4L * attended));
            summary.setNeededClasses((int) needed);
            summary.setCanBunkClasses(0);
        } else if (total > 0) {
            // Can skip without dropping below 75%:
            // attended / (total + y) >= 0.75 => y <= (4*attended - 3*total) / 3
            long canSkip = Math.max(0, ((4L * attended) - (3L * total)) / 3L);
            summary.setCanBunkClasses((int) canSkip);
            summary.setNeededClasses(0);
        } else {
            summary.setNeededClasses(0);
            summary.setCanBunkClasses(0);
        }

        // Subject-wise breakdowns
        Map<Course, List<Attendance>> byCourse = allRecords.stream()
            .collect(Collectors.groupingBy(Attendance::getCourse));

        List<SubjectAttendanceDto> subjectList = new ArrayList<>();
        // Also include enrolled courses that might have 0 attendance records yet
        Set<Course> coursesToConsider = new HashSet<>(student.getEnrolledCourses());
        coursesToConsider.addAll(byCourse.keySet());

        for (Course c : coursesToConsider) {
            List<Attendance> cRecords = byCourse.getOrDefault(c, Collections.emptyList());
            int cTotal = cRecords.size();
            int cAttended = (int) cRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE || a.getStatus() == AttendanceStatus.EXCUSED)
                .count();
            int cAbsent = (int) cRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();
            double cPct = cTotal > 0 ? Math.round((cAttended * 100.0 / cTotal) * 10.0) / 10.0 : 100.0;

            String badge = cPct >= 75.0 ? "GOOD" : (cPct >= 65.0 ? "WARNING" : "CRITICAL");
            subjectList.add(new SubjectAttendanceDto(
                c.getId(),
                c.getCourseCode(),
                c.getCourseName(),
                cTotal,
                cAttended,
                cAbsent,
                cPct,
                badge
            ));
        }

        subjectList.sort(Comparator.comparing(SubjectAttendanceDto::getCourseCode));
        summary.setSubjectAttendance(subjectList);

        return summary;
    }

    public List<AttendanceRecordDto> getStudentAttendanceHistory(Long studentId, Long courseId) {
        List<Attendance> records;
        if (courseId != null) {
            records = attendanceRepository.findByStudentIdAndCourseIdOrderByAttendanceDateDesc(studentId, courseId);
        } else {
            records = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
        }
        return records.stream().map(this::toRecordDto).collect(Collectors.toList());
    }

    public ReportSummaryDto generateReport(Long courseId, Long studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = attendanceRepository.filterAttendance(courseId, studentId, startDate, endDate);

        ReportSummaryDto report = new ReportSummaryDto();
        report.setTitle("Student Attendance Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        if (courseId != null) {
            courseRepository.findById(courseId).ifPresent(c -> {
                report.setCourseName(c.getCourseName());
                report.setCourseCode(c.getCourseCode());
            });
        }

        int total = records.size();
        int present = 0;
        int absent = 0;
        int late = 0;
        int excused = 0;

        for (Attendance a : records) {
            switch (a.getStatus()) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case LATE -> late++;
                case EXCUSED -> excused++;
            }
        }

        int attended = present + late + excused;
        double overallPct = total > 0 ? Math.round((attended * 100.0 / total) * 10.0) / 10.0 : 100.0;

        report.setTotalRecords(total);
        report.setTotalPresent(present);
        report.setTotalAbsent(absent);
        report.setTotalLate(late);
        report.setTotalExcused(excused);
        report.setOverallPercentage(overallPct);
        report.setRecords(records.stream().map(this::toRecordDto).collect(Collectors.toList()));

        // Group by students to build student summaries for this report
        Map<Student, List<Attendance>> byStudent = records.stream()
            .collect(Collectors.groupingBy(Attendance::getStudent));

        List<AttendanceSummaryDto> studentSummaries = new ArrayList<>();
        for (Map.Entry<Student, List<Attendance>> entry : byStudent.entrySet()) {
            Student s = entry.getKey();
            List<Attendance> sList = entry.getValue();
            int sTotal = sList.size();
            int sPres = (int) sList.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            int sLate = (int) sList.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            int sAbs = (int) sList.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            int sExc = (int) sList.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
            int sAtt = sPres + sLate + sExc;
            double sPct = sTotal > 0 ? Math.round((sAtt * 100.0 / sTotal) * 10.0) / 10.0 : 100.0;

            AttendanceSummaryDto sSum = new AttendanceSummaryDto();
            sSum.setStudentId(s.getId());
            sSum.setRollNumber(s.getRollNumber());
            sSum.setStudentName(s.getFullName());
            sSum.setDepartment(s.getDepartment());
            sSum.setSemester(s.getSemester());
            sSum.setTotalClasses(sTotal);
            sSum.setAttendedClasses(sAtt);
            sSum.setPresentClasses(sPres);
            sSum.setAbsentClasses(sAbs);
            sSum.setLateClasses(sLate);
            sSum.setExcusedClasses(sExc);
            sSum.setAttendancePercentage(sPct);
            sSum.setShortage(sPct < 75.0);
            sSum.setStatusBadge(sPct >= 75.0 ? "GOOD" : (sPct >= 65.0 ? "WARNING" : "CRITICAL"));
            studentSummaries.add(sSum);
        }

        studentSummaries.sort(Comparator.comparing(AttendanceSummaryDto::getRollNumber));
        report.setStudentSummaries(studentSummaries);

        return report;
    }

    public String exportReportCsv(Long courseId, Long studentId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = attendanceRepository.filterAttendance(courseId, studentId, startDate, endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Date,Course Code,Course Name,Roll Number,Student Name,Department,Semester,Status,Remarks\n");

        for (Attendance a : records) {
            sb.append(a.getId()).append(",")
              .append(a.getAttendanceDate()).append(",")
              .append("\"").append(escapeCsv(a.getCourse().getCourseCode())).append("\",")
              .append("\"").append(escapeCsv(a.getCourse().getCourseName())).append("\",")
              .append("\"").append(escapeCsv(a.getStudent().getRollNumber())).append("\",")
              .append("\"").append(escapeCsv(a.getStudent().getFullName())).append("\",")
              .append("\"").append(escapeCsv(a.getStudent().getDepartment())).append("\",")
              .append(a.getStudent().getSemester()).append(",")
              .append(a.getStatus().name()).append(",")
              .append("\"").append(escapeCsv(a.getRemarks() != null ? a.getRemarks() : "")).append("\"\n");
        }

        return sb.toString();
    }

    public List<AttendanceSummaryDto> getShortageList(Long courseId, double threshold) {
        List<Student> students = (courseId != null)
            ? studentRepository.findByCourseId(courseId)
            : studentRepository.findAllOrderByRollNumber();

        List<AttendanceSummaryDto> shortageList = new ArrayList<>();
        for (Student s : students) {
            AttendanceSummaryDto summary = getStudentOverallAttendance(s.getId());
            if (summary.getAttendancePercentage() < threshold && summary.getTotalClasses() > 0) {
                shortageList.add(summary);
            }
        }
        return shortageList;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    private AttendanceRecordDto toRecordDto(Attendance a) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(a.getId());
        dto.setStudentId(a.getStudent().getId());
        dto.setRollNumber(a.getStudent().getRollNumber());
        dto.setStudentName(a.getStudent().getFullName());
        dto.setCourseId(a.getCourse().getId());
        dto.setCourseCode(a.getCourse().getCourseCode());
        dto.setCourseName(a.getCourse().getCourseName());
        dto.setAttendanceDate(a.getAttendanceDate());
        dto.setStatus(a.getStatus());
        dto.setRemarks(a.getRemarks());
        dto.setMarkedAt(a.getMarkedAt());
        return dto;
    }
}
