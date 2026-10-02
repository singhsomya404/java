package com.attendance.dto;

public class SubjectAttendanceDto {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private int totalClasses;
    private int attendedClasses;
    private int absentClasses;
    private double percentage;
    private String statusBadge; // "GOOD", "WARNING", "CRITICAL"

    public SubjectAttendanceDto() {}

    public SubjectAttendanceDto(Long courseId, String courseCode, String courseName,
                                int totalClasses, int attendedClasses, int absentClasses,
                                double percentage, String statusBadge) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
        this.absentClasses = absentClasses;
        this.percentage = percentage;
        this.statusBadge = statusBadge;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = attendedClasses;
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public void setAbsentClasses(int absentClasses) {
        this.absentClasses = absentClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getStatusBadge() {
        return statusBadge;
    }

    public void setStatusBadge(String statusBadge) {
        this.statusBadge = statusBadge;
    }
}
