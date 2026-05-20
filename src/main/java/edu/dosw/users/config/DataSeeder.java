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
 * Carga el stub en memoria del servicio de identidad con un usuario de prueba fijo al inicio.
 *
 * <p>Activo únicamente en perfiles no productivos. El usuario sembrado siempre recibe
 * id=1 (el contador del stub comienza en 1), por lo que las pruebas de integración en
 * Postman pueden referenciar un {@code userId} predecible sin necesidad de llamar a
 * un endpoint de creación.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
@Profile("!prod")
public class DataSeeder implements ApplicationRunner {

    private final IdentityServiceClient identityServiceClient;

    public DataSeeder(IdentityServiceClient identityServiceClient) {
        this.identityServiceClient = identityServiceClient;
    }

    /**
     * Ejecuta la siembra de datos al arrancar la aplicación creando el usuario de prueba
     * predefinido en el stub del servicio de identidad.
     *
     * @param args argumentos de la aplicación (no utilizados)
     */
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