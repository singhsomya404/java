package com.attendance;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.MarkAttendanceRequest;
import com.attendance.dto.StudentDto;
import com.attendance.model.AttendanceStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class StudentAttendanceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test Admin Login - Success")
    void testAdminLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("Test Student Login - Success")
    void testStudentLogin() throws Exception {
        LoginRequest req = new LoginRequest("CS202601", "student123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.role", is("STUDENT")))
                .andExpect(jsonPath("$.rollNumber", is("CS202601")));
    }

    @Test
    @DisplayName("Test Add Student API")
    void testAddStudent() throws Exception {
        StudentDto student = new StudentDto();
        student.setRollNumber("CS202699");
        student.setFullName("Test Student");
        student.setEmail("test.student@college.edu");
        student.setDepartment("Computer Science");
        student.setSemester(4);
        student.setSection("A");
        student.setPassword("student123");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rollNumber", is("CS202699")))
                .andExpect(jsonPath("$.fullName", is("Test Student")));
    }

    @Test
    @DisplayName("Test Mark Attendance API")
    void testMarkAttendance() throws Exception {
        MarkAttendanceRequest req = new MarkAttendanceRequest();
        req.setCourseId(1L);
        req.setAttendanceDate(LocalDate.now());

        MarkAttendanceRequest.StudentEntry entry = new MarkAttendanceRequest.StudentEntry(
                1L, AttendanceStatus.PRESENT, "Present in class"
        );
        req.setEntries(List.of(entry));

        mockMvc.perform(post("/api/attendance/mark")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.count", is(1)));
    }

    @Test
    @DisplayName("Test View Attendance Percentage and Summary")
    void testViewAttendanceSummary() throws Exception {
        mockMvc.perform(get("/api/attendance/student/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is(1)))
                .andExpect(jsonPath("$.attendancePercentage", notNullValue()))
                .andExpect(jsonPath("$.subjectAttendance", notNullValue()));
    }

    @Test
    @DisplayName("Test Attendance Report Generation")
    void testGenerateAttendanceReport() throws Exception {
        mockMvc.perform(get("/api/attendance/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.overallPercentage", notNullValue()));
    }

    @Test
    @DisplayName("Test Export Attendance Report to CSV")
    void testExportCsvReport() throws Exception {
        mockMvc.perform(get("/api/attendance/report/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".csv")))
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("Test Defaulter / Shortage List API")
    void testShortageList() throws Exception {
        mockMvc.perform(get("/api/attendance/shortage?threshold=75.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
