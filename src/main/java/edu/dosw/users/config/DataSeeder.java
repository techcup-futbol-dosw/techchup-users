package edu.dosw.users.config;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.model.UserModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds the in-memory identity stub with a fixed test user on startup.
 *
 * <p>Active only in non-production profiles. The seeded user always receives
 * id=1 (the stub's counter starts at 1), so Postman integration tests can
 * reference a predictable {@code userId} without needing to call a create
 * endpoint.</p>
 */
@Component
@Profile("!prod")
public class DataSeeder implements ApplicationRunner {

    private final IdentityServiceClient identityServiceClient;

    public DataSeeder(IdentityServiceClient identityServiceClient) {
        this.identityServiceClient = identityServiceClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserModel testUser = UserModel.builder()
                .fullName("Carlos Rodríguez")
                .email("carlos.rodriguez@escuelaing.edu.co")
                .password("Password123")
                .identification("1234567890")
                .birthDate(LocalDate.of(2000, 5, 15))
                .gender(Gender.MALE)
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingeniería de Sistemas")
                .semester(6)
                .build();

        identityServiceClient.createUser(testUser);
    }
}