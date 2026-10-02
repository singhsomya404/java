package com.attendance.dto;

public class CourseDto {
    private Long id;
    private String courseCode;
    private String courseName;
    private String department;
    private Integer semester;
    private int enrolledStudentCount;

    public CourseDto() {}

    public CourseDto(Long id, String courseCode, String courseName, String department, Integer semester, int enrolledStudentCount) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.semester = semester;
        this.enrolledStudentCount = enrolledStudentCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getEnrolledStudentCount() {
        return enrolledStudentCount;
    }

    public void setEnrolledStudentCount(int enrolledStudentCount) {
        this.enrolledStudentCount = enrolledStudentCount;
    }
}
