package com.attendance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class StudentAttendanceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context, entities, repositories, and DataInitializer
    }
}
