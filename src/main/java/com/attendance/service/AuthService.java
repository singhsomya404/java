package com.attendance.service;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.model.Role;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AuthService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public LoginResponse authenticate(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return LoginResponse.failure("Username and password are required.");
        }

        String usernameOrRoll = request.getUsername().trim();
        String password = request.getPassword().trim();

        // 1. Try finding by direct username
        Optional<User> userOpt = userRepository.findByUsername(usernameOrRoll);

        // 2. If not found, try finding student by roll number or email
        if (userOpt.isEmpty()) {
            Optional<Student> studentOpt = studentRepository.findByRollNumber(usernameOrRoll);
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findByEmail(usernameOrRoll);
            }
            if (studentOpt.isPresent() && studentOpt.get().getUser() != null) {
                userOpt = Optional.of(studentOpt.get().getUser());
            }
        }

        if (userOpt.isEmpty()) {
            return LoginResponse.failure("Invalid username or roll number.");
        }

        User user = userOpt.get();

        // Check password match (plain text check for simplicity and compatibility with seed data)
        if (!user.getPassword().equals(password)) {
            return LoginResponse.failure("Incorrect password. Please try again.");
        }

        Long studentId = null;
        String rollNumber = null;

        if (user.getRole() == Role.STUDENT) {
            Optional<Student> student = studentRepository.findByUser_Username(user.getUsername());
            if (student.isPresent()) {
                studentId = student.get().getId();
                rollNumber = student.get().getRollNumber();
            }
        }

        return LoginResponse.success(
            user.getRole(),
            user.getUsername(),
            user.getFullName(),
            studentId,
            rollNumber
        );
    }
}
