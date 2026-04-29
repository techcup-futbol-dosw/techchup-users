package edu.dosw.users;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test for the Spring Boot application context.
 *
 * <p>Verifies that the application can start with the test configuration and
 * that all required beans can be created successfully.</p>
 */
@SpringBootTest
class AppTest {

    /**
     * Loads the application context without executing additional assertions.
     */
    @Test
    void contextLoads() {
    }
}
