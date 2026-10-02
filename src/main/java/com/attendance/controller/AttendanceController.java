package com.attendance.controller;

import com.attendance.dto.AttendanceRecordDto;
import com.attendance.dto.AttendanceSummaryDto;
import com.attendance.dto.MarkAttendanceRequest;
import com.attendance.dto.ReportSummaryDto;
import com.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody MarkAttendanceRequest request) {
        try {
            List<AttendanceRecordDto> saved = attendanceService.markAttendance(request);
            return ResponseEntity.ok(Map.of(
                "message", "Attendance marked successfully!",
                "count", saved.size(),
                "records", saved
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sheet")
    public ResponseEntity<?> getAttendanceSheet(
            @RequestParam Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Map<String, Object>> sheet = attendanceService.getAttendanceSheet(courseId, date);
            return ResponseEntity.ok(sheet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<?> getStudentSummary(@PathVariable Long studentId) {
        try {
            AttendanceSummaryDto summary = attendanceService.getStudentOverallAttendance(studentId);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<?> getStudentHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId) {
        try {
            List<AttendanceRecordDto> history = attendanceService.getStudentAttendanceHistory(studentId, courseId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report")
    public ResponseEntity<?> getReport(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ReportSummaryDto report = attendanceService.generateReport(courseId, studentId, startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/export-csv")
    public ResponseEntity<byte[]> exportReportCsv(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csvData = attendanceService.exportReportCsv(courseId, studentId, startDate, endDate);
        byte[] bytes = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = "attendance_report_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    @GetMapping("/shortage")
    public ResponseEntity<?> getShortageList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "75.0") double threshold) {
        try {
            List<AttendanceSummaryDto> shortageList = attendanceService.getShortageList(courseId, threshold);
            return ResponseEntity.ok(shortageList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
