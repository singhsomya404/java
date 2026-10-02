-- ====================================================================
-- Student Attendance Management System - Database Schema & Initial Data
-- Database Name: attendance_db
-- Run this script in MySQL Workbench or via MySQL CLI:
-- mysql -u root -p < database/schema.sql
-- ====================================================================

CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

-- 1. Users Table (Admin & Student Accounts)
CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'STUDENT'
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Students Table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    section VARCHAR(10) DEFAULT 'A',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE SET NULL
);

-- 3. Courses / Subjects Table
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Student Course Enrollments
CREATE TABLE IF NOT EXISTS student_courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);

-- 5. Attendance Records Table
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'PRESENT', 'ABSENT', 'LATE', 'EXCUSED'
    remarks VARCHAR(255),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_att_record UNIQUE (student_id, course_id, attendance_date)
);

-- ====================================================================
-- Sample Seed Data
-- ====================================================================

-- Insert Default Admin (admin / admin123)
INSERT INTO app_users (username, password, role, full_name)
SELECT 'admin', 'admin123', 'ADMIN', 'System Administrator'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- Insert Default Student User Accounts
INSERT INTO app_users (username, password, role, full_name)
SELECT 'CS202601', 'student123', 'STUDENT', 'Aarav Sharma'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'CS202601');

INSERT INTO app_users (username, password, role, full_name)
SELECT 'CS202602', 'student123', 'STUDENT', 'Diya Patel'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'CS202602');

INSERT INTO app_users (username, password, role, full_name)
SELECT 'CS202603', 'student123', 'STUDENT', 'Rohan Gupta'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'CS202603');

INSERT INTO app_users (username, password, role, full_name)
SELECT 'CS202604', 'student123', 'STUDENT', 'Ananya Singh'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'CS202604');

INSERT INTO app_users (username, password, role, full_name)
SELECT 'CS202605', 'student123', 'STUDENT', 'Kabir Verma'
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'CS202605');

-- Insert Sample Students
INSERT INTO students (user_id, roll_number, full_name, email, phone, department, semester, section)
SELECT u.id, 'CS202601', 'Aarav Sharma', 'aarav.sharma@college.edu', '+91 9876543210', 'Computer Science', 4, 'A'
FROM app_users u WHERE u.username = 'CS202601'
AND NOT EXISTS (SELECT 1 FROM students WHERE roll_number = 'CS202601');

INSERT INTO students (user_id, roll_number, full_name, email, phone, department, semester, section)
SELECT u.id, 'CS202602', 'Diya Patel', 'diya.patel@college.edu', '+91 9876543211', 'Computer Science', 4, 'A'
FROM app_users u WHERE u.username = 'CS202602'
AND NOT EXISTS (SELECT 1 FROM students WHERE roll_number = 'CS202602');

INSERT INTO students (user_id, roll_number, full_name, email, phone, department, semester, section)
SELECT u.id, 'CS202603', 'Rohan Gupta', 'rohan.gupta@college.edu', '+91 9876543212', 'Computer Science', 4, 'A'
FROM app_users u WHERE u.username = 'CS202603'
AND NOT EXISTS (SELECT 1 FROM students WHERE roll_number = 'CS202603');

INSERT INTO students (user_id, roll_number, full_name, email, phone, department, semester, section)
SELECT u.id, 'CS202604', 'Ananya Singh', 'ananya.singh@college.edu', '+91 9876543213', 'Computer Science', 4, 'B'
FROM app_users u WHERE u.username = 'CS202604'
AND NOT EXISTS (SELECT 1 FROM students WHERE roll_number = 'CS202604');

INSERT INTO students (user_id, roll_number, full_name, email, phone, department, semester, section)
SELECT u.id, 'CS202605', 'Kabir Verma', 'kabir.verma@college.edu', '+91 9876543214', 'Computer Science', 4, 'B'
FROM app_users u WHERE u.username = 'CS202605'
AND NOT EXISTS (SELECT 1 FROM students WHERE roll_number = 'CS202605');

-- Insert Sample Courses
INSERT INTO courses (course_code, course_name, department, semester)
SELECT 'CS401', 'Database Management Systems', 'Computer Science', 4
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE course_code = 'CS401');

INSERT INTO courses (course_code, course_name, department, semester)
SELECT 'CS402', 'Design and Analysis of Algorithms', 'Computer Science', 4
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE course_code = 'CS402');

INSERT INTO courses (course_code, course_name, department, semester)
SELECT 'CS403', 'Operating Systems', 'Computer Science', 4
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE course_code = 'CS403');

INSERT INTO courses (course_code, course_name, department, semester)
SELECT 'CS404', 'Software Engineering & Web Tech', 'Computer Science', 4
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE course_code = 'CS404');

-- Enroll Students into Courses
INSERT IGNORE INTO student_courses (student_id, course_id)
SELECT s.id, c.id FROM students s CROSS JOIN courses c;

-- Insert Seed Attendance Records (Historical dates)
-- CS401 Dates: 2026-09-20 to 2026-10-01
INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-22', 'PRESENT', 'On time'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number IN ('CS202601', 'CS202602', 'CS202603', 'CS202604');

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-22', 'ABSENT', 'Unexcused'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number = 'CS202605';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-24', 'PRESENT', 'Regular'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number IN ('CS202601', 'CS202602', 'CS202604', 'CS202605');

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-24', 'ABSENT', 'Medical leave'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number = 'CS202603';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-26', 'PRESENT', 'Regular'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number IN ('CS202601', 'CS202602', 'CS202603');

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-26', 'LATE', 'Arrived 15 mins late'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number = 'CS202604';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-26', 'ABSENT', 'No show'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number = 'CS202605';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-29', 'PRESENT', 'Active participation'
FROM students s, courses c WHERE c.course_code = 'CS401';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-10-01', 'PRESENT', 'Regular'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number IN ('CS202601', 'CS202602', 'CS202603');

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-10-01', 'ABSENT', 'Leave application'
FROM students s, courses c WHERE c.course_code = 'CS401' AND s.roll_number IN ('CS202604', 'CS202605');

-- CS402 Seed Attendance
INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-23', 'PRESENT', 'On time'
FROM students s, courses c WHERE c.course_code = 'CS402';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-25', 'PRESENT', 'Lab session completed'
FROM students s, courses c WHERE c.course_code = 'CS402' AND s.roll_number != 'CS202603';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-25', 'ABSENT', 'Absent'
FROM students s, courses c WHERE c.course_code = 'CS402' AND s.roll_number = 'CS202603';

INSERT IGNORE INTO attendance (student_id, course_id, attendance_date, status, remarks)
SELECT s.id, c.id, '2026-09-30', 'PRESENT', 'Lab session completed'
FROM students s, courses c WHERE c.course_code = 'CS402';
