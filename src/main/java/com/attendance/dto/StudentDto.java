package com.attendance.dto;

import java.util.Set;

public class StudentDto {
    private Long id;
    private String rollNumber;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private Integer semester;
    private String section;
    private String password;
    private Set<Long> enrolledCourseIds;
    private Long userId;

    public StudentDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Long> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(Set<Long> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
