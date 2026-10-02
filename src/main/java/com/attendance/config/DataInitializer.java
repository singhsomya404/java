package com.attendance.config;

import com.attendance.model.*;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.CourseRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           CourseRepository courseRepository,
                           AttendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin123", Role.ADMIN, "System Administrator");
            userRepository.save(admin);
        }

        // 2. Seed Courses
        List<Course> courses = new ArrayList<>();
        if (courseRepository.count() == 0) {
            courses.add(courseRepository.save(new Course("CS401", "Database Management Systems", "Computer Science", 4)));
            courses.add(courseRepository.save(new Course("CS402", "Design and Analysis of Algorithms", "Computer Science", 4)));
            courses.add(courseRepository.save(new Course("CS403", "Operating Systems", "Computer Science", 4)));
            courses.add(courseRepository.save(new Course("CS404", "Software Engineering & Web Tech", "Computer Science", 4)));
        } else {
            courses = courseRepository.findAll();
        }

        // 3. Seed Students
        if (studentRepository.count() == 0) {
            String[][] studentData = {
                {"CS202601", "Aarav Sharma", "aarav.sharma@college.edu", "+91 9876543210", "Computer Science", "4", "A"},
                {"CS202602", "Diya Patel", "diya.patel@college.edu", "+91 9876543211", "Computer Science", "4", "A"},
                {"CS202603", "Rohan Gupta", "rohan.gupta@college.edu", "+91 9876543212", "Computer Science", "4", "A"},
                {"CS202604", "Ananya Singh", "ananya.singh@college.edu", "+91 9876543213", "Computer Science", "4", "B"},
                {"CS202605", "Kabir Verma", "kabir.verma@college.edu", "+91 9876543214", "Computer Science", "4", "B"}
            };

            List<Student> savedStudents = new ArrayList<>();
            for (String[] data : studentData) {
                String roll = data[0];
                String name = data[1];
                String email = data[2];
                String phone = data[3];
                String dept = data[4];
                int sem = Integer.parseInt(data[5]);
                String sec = data[6];

                User u = userRepository.findByUsername(roll).orElseGet(() ->
                    userRepository.save(new User(roll, "student123", Role.STUDENT, name))
                );

                Student s = new Student(roll, name, email, phone, dept, sem, sec);
                s.setUser(u);
                s.setEnrolledCourses(new HashSet<>(courses));
                savedStudents.add(studentRepository.save(s));
            }

            // 4. Seed Attendance Records for historical dates
            if (!courses.isEmpty() && !savedStudents.isEmpty()) {
                Course dbms = courses.stream().filter(c -> "CS401".equals(c.getCourseCode())).findFirst().orElse(courses.get(0));
                Course algo = courses.stream().filter(c -> "CS402".equals(c.getCourseCode())).findFirst().orElse(courses.get(0));

                LocalDate[] dbmsDates = {
                    LocalDate.of(2026, 9, 20),
                    LocalDate.of(2026, 9, 22),
                    LocalDate.of(2026, 9, 24),
                    LocalDate.of(2026, 9, 26),
                    LocalDate.of(2026, 9, 29),
                    LocalDate.of(2026, 10, 1)
                };

                for (LocalDate d : dbmsDates) {
                    for (int i = 0; i < savedStudents.size(); i++) {
                        Student s = savedStudents.get(i);
                        AttendanceStatus st = AttendanceStatus.PRESENT;
                        String remark = "Regular";

                        // Make realistic variation so percentages vary across students (e.g. Diya 100%, Kabir 60%)
                        if (i == 4 && (d.getDayOfMonth() == 22 || d.getDayOfMonth() == 26 || d.getDayOfMonth() == 1)) {
                            st = AttendanceStatus.ABSENT;
                            remark = "Unexcused Absence";
                        } else if (i == 2 && d.getDayOfMonth() == 24) {
                            st = AttendanceStatus.ABSENT;
                            remark = "Medical Leave";
                        } else if (i == 3 && d.getDayOfMonth() == 26) {
                            st = AttendanceStatus.LATE;
                            remark = "10 mins late";
                        }

                        attendanceRepository.save(new Attendance(s, dbms, d, st, remark));
                    }
                }

                // Algo dates
                LocalDate[] algoDates = {
                    LocalDate.of(2026, 9, 23),
                    LocalDate.of(2026, 9, 25),
                    LocalDate.of(2026, 9, 30)
                };

                for (LocalDate d : algoDates) {
                    for (int i = 0; i < savedStudents.size(); i++) {
                        Student s = savedStudents.get(i);
                        AttendanceStatus st = AttendanceStatus.PRESENT;
                        String remark = "Attended lab";
                        if (i == 2 && d.getDayOfMonth() == 25) {
                            st = AttendanceStatus.ABSENT;
                            remark = "Absent";
                        }
                        attendanceRepository.save(new Attendance(s, algo, d, st, remark));
                    }
                }
            }
        }
    }
}
