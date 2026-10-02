package com.attendance.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportSummaryDto {
    private String title;
    private String courseName;
    private String courseCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalRecords;
    private int totalPresent;
    private int totalAbsent;
    private int totalLate;
    private int totalExcused;
    private double overallPercentage;
    private List<AttendanceRecordDto> records = new ArrayList<>();
    private List<AttendanceSummaryDto> studentSummaries = new ArrayList<>();

    public ReportSummaryDto() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getTotalPresent() {
        return totalPresent;
    }

    public void setTotalPresent(int totalPresent) {
        this.totalPresent = totalPresent;
    }

    public int getTotalAbsent() {
        return totalAbsent;
    }

    public void setTotalAbsent(int totalAbsent) {
        this.totalAbsent = totalAbsent;
    }

    public int getTotalLate() {
        return totalLate;
    }

    public void setTotalLate(int totalLate) {
        this.totalLate = totalLate;
    }

    public int getTotalExcused() {
        return totalExcused;
    }

    public void setTotalExcused(int totalExcused) {
        this.totalExcused = totalExcused;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }

    public List<AttendanceRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordDto> records) {
        this.records = records;
    }

    public List<AttendanceSummaryDto> getStudentSummaries() {
        return studentSummaries;
    }

    public void setStudentSummaries(List<AttendanceSummaryDto> studentSummaries) {
        this.studentSummaries = studentSummaries;
    }
}
