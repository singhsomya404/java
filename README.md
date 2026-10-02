# 🎓 Student Attendance Management System (SAMS)

A complete, production-ready Student Attendance Management System built with a **Java Spring Boot backend**, **MySQL database**, and a modern, responsive **HTML5/CSS3/JavaScript frontend**.

---

## 🌟 Key Features

1. **Student & Admin Authentication**:
   - Role-based login for **Admin / Teachers** and **Students**.
   - Student login using **Roll Number** or Username.
   - Quick one-click demo credentials chip on login screen.

2. **Add & Manage Students**:
   - Register new students with Roll Number, Full Name, Email, Phone, Department, Semester, Section, and initial login password.
   - Automatic user account creation and automated course enrollment.
   - Search students dynamically by Roll Number, Name, or Department.
   - Individual student attendance profile view and student deletion.

3. **Mark Attendance Sheet**:
   - Select Subject/Course and Lecture Date (defaults to today).
   - Fast batch actions: **"Mark All Present"** and **"Mark All Absent"**.
   - Interactive badge toggle switches per student: `PRESENT`, `ABSENT`, `LATE`, `EXCUSED`.
   - Optional remarks input per student (e.g., medical leave, late arrival).
   - Instant save and update support for past or current dates.

4. **View Attendance Percentage & Smart Advisor**:
   - **Student Dashboard**:
     - Animated **Radial Percentage Gauge** reflecting overall attendance rate.
     - **Color-coded status**:
       - 🟢 **Good (>= 75%)**: Mandatory college requirement satisfied.
       - 🟡 **Warning (65% - 74.9%)**: Shortage alert.
       - 🔴 **Critical (< 65%)**: Severe attendance deficit.
     - **Smart Advisor / Target Calculator**:
       - If `>= 75%`: Calculates exactly how many classes the student can safely skip while remaining above 75%.
       - If `< 75%`: Calculates exactly how many consecutive upcoming classes must be attended to cross 75%.
     - **Subject-wise Breakdown Cards**: Classes attended vs total conducted with visual progress bars.
   - **Admin Defaulters & Shortage Monitor**:
     - Class-wide percentage list filtered by threshold (75%, 80%, 65%).
     - Identifies students facing shortage with recovery targets.

5. **Generate Reports & CSV Export**:
   - Date range filters, Course filter, and Student filter.
   - Real-time aggregate statistics (Total Records, Total Present, Total Absent, Overall %).
   - Detailed attendance logs table.
   - **Export to CSV**: Instant download for spreadsheet analysis (Excel/Google Sheets).
   - **Print Report**: Formatted clean printable report view.

---

## 🛠️ Technology Stack

- **Backend**: Java 21 / 25, Spring Boot 3.3.4 (REST API, Spring Data JPA, Hibernate, Validation)
- **Database**: MySQL 8.0 (`attendance_db`), HikariCP connection pooling
- **Frontend**: HTML5, Modern CSS3 (Variables, Flexbox/Grid, Glassmorphic accents), Vanilla JavaScript (ES6+ Fetch API, DOM manipulation)
- **Build Tool**: Apache Maven (portable Maven wrapper included)

---

## 🚀 Quick Start Guide

### 1. Database Setup in MySQL

You can set up the database using MySQL Workbench or the MySQL command line:

```bash
# Connect to MySQL and run the schema file:
mysql -u root -p < database/schema.sql
```

The database script creates:
- Database: `attendance_db`
- Tables: `app_users`, `students`, `courses`, `student_courses`, `attendance`
- Preloaded sample data (Admin account, 5 sample students, 4 courses, and attendance logs)

### 2. Configure Database Credentials

Open `src/main/resources/application.properties` and verify your MySQL password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```
*(You can also set the environment variable `DB_PASSWORD` or modify the password in `application.properties`)*.

### 3. Run the Application

Execute the one-click startup script:

```bash
# In PowerShell or Command Prompt:
.\run.bat
```

Or run via Maven wrapper:

```bash
.\mvnw.bat spring-boot:run
```

> **Zero-Setup Demo Mode (H2 in-memory fallback)**:
> If you wish to test immediately without configuring MySQL, simply run:
> ```bash
> .\mvnw.bat spring-boot:run -Dspring-boot.run.profiles=h2
> ```

### 4. Access the Web Dashboard

Open your browser and navigate to:
👉 **`http://localhost:8080`**

---

## 🔑 Default Login Credentials

| Role | Username / Roll No | Password | Description |
|---|---|---|---|
| **Admin / Faculty** | `admin` | `admin123` | Full administrative access, add students, mark attendance, reports |
| **Student (High Att.)** | `CS202601` | `student123` | Aarav Sharma (Good standing ~88%) |
| **Student (Shortage)** | `CS202605` | `student123` | Kabir Verma (Shortage <60%) |
| **Student (Warning)** | `CS202603` | `student123` | Rohan Gupta |

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user (Admin or Student) |
| `GET` | `/api/students` | Get all students (optional `?department=&semester=`) |
| `POST` | `/api/students` | Register a new student |
| `PUT` | `/api/students/{id}` | Update student profile |
| `DELETE` | `/api/students/{id}` | Delete student |
| `GET` | `/api/courses` | List all available subjects/courses |
| `POST` | `/api/courses` | Add a new course |
| `POST` | `/api/attendance/mark` | Record/update attendance for a course lecture |
| `GET` | `/api/attendance/sheet` | Fetch attendance sheet for `?courseId=&date=` |
| `GET` | `/api/attendance/student/{id}/summary` | Get student percentage, shortage status, and subject breakdown |
| `GET` | `/api/attendance/student/{id}/history` | Get date-wise attendance records for student |
| `GET` | `/api/attendance/report` | Query attendance report with optional filters |
| `GET` | `/api/attendance/report/export-csv` | Download CSV attendance report |
| `GET` | `/api/attendance/shortage` | Get list of students with attendance below threshold |
