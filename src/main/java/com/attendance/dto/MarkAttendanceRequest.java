package com.attendance.dto;

import com.attendance.model.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;

public class MarkAttendanceRequest {
    private Long courseId;
    private LocalDate attendanceDate;
    private List<StudentEntry> entries;

    public MarkAttendanceRequest() {}

    public static class StudentEntry {
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;

        public StudentEntry() {}

        public StudentEntry(Long studentId, AttendanceStatus status, String remarks) {
            this.studentId = studentId;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<StudentEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<StudentEntry> entries) {
        this.entries = entries;
    }
}
