package com.attendance.dto;

import com.attendance.model.Role;

public class LoginResponse {
    private boolean success;
    private String message;
    private Role role;
    private String username;
    private String fullName;
    private Long studentId;
    private String rollNumber;

    public LoginResponse() {}

    public static LoginResponse failure(String message) {
        LoginResponse resp = new LoginResponse();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

    public static LoginResponse success(Role role, String username, String fullName, Long studentId, String rollNumber) {
        LoginResponse resp = new LoginResponse();
        resp.setSuccess(true);
        resp.setMessage("Login successful");
        resp.setRole(role);
        resp.setUsername(username);
        resp.setFullName(fullName);
        resp.setStudentId(studentId);
        resp.setRollNumber(rollNumber);
        return resp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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
}
