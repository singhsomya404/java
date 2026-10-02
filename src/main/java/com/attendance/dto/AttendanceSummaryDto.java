package com.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class AttendanceSummaryDto {
    private Long studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private Integer semester;

    private int totalClasses;
    private int attendedClasses;
    private int presentClasses;
    private int absentClasses;
    private int lateClasses;
    private int excusedClasses;

    private double attendancePercentage;
    private String statusBadge; // "GOOD", "WARNING", "CRITICAL"
    private boolean shortage;

    private int neededClasses;
    private int canBunkClasses;

    private List<SubjectAttendanceDto> subjectAttendance = new ArrayList<>();

    public AttendanceSummaryDto() {}

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
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

    public int getPresentClasses() {
        return presentClasses;
    }

    public void setPresentClasses(int presentClasses) {
        this.presentClasses = presentClasses;
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public void setAbsentClasses(int absentClasses) {
        this.absentClasses = absentClasses;
    }

    public int getLateClasses() {
        return lateClasses;
    }

    public void setLateClasses(int lateClasses) {
        this.lateClasses = lateClasses;
    }

    public int getExcusedClasses() {
        return excusedClasses;
    }

    public void setExcusedClasses(int excusedClasses) {
        this.excusedClasses = excusedClasses;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public String getStatusBadge() {
        return statusBadge;
    }

    public void setStatusBadge(String statusBadge) {
        this.statusBadge = statusBadge;
    }

    public boolean isShortage() {
        return shortage;
    }

    public void setShortage(boolean shortage) {
        this.shortage = shortage;
    }

    public int getNeededClasses() {
        return neededClasses;
    }

    public void setNeededClasses(int neededClasses) {
        this.neededClasses = neededClasses;
    }

    public int getCanBunkClasses() {
        return canBunkClasses;
    }

    public void setCanBunkClasses(int canBunkClasses) {
        this.canBunkClasses = canBunkClasses;
    }

    public List<SubjectAttendanceDto> getSubjectAttendance() {
        return subjectAttendance;
    }

    public void setSubjectAttendance(List<SubjectAttendanceDto> subjectAttendance) {
        this.subjectAttendance = subjectAttendance;
    }
}
