package edu.dosw.users.config;

import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga la base de datos H2 con un usuario de prueba fijo al inicio.
 *
 * <p>Activo únicamente en perfiles no productivos. El usuario sembrado recibe
 * id=1, por lo que las pruebas de integración en Postman pueden referenciar
 * un {@code userId} predecible sin necesidad de llamar a un endpoint de creación.</p>
 */
@Component
@Profile("!prod")
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserEntity testUser = UserEntity.builder()
                .id(1L)
                .fullName("Carlos Rodríguez")
                .email("carlos.rodriguez@escuelaing.edu.co")
                .password("Password123")
                .identification("1234567890")
                .birthDate(LocalDate.of(2000, 5, 15))
                .gender(Gender.MALE)
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingeniería de Sistemas")
                .semester(6)
                .status("ACTIVE")
                .profileCreatedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(testUser);
    }
}
