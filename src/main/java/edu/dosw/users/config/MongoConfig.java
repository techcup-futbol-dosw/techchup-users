package edu.dosw.users.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * Configuración del cliente MongoDB para el perfil de producción.
 *
 * <p>Lee el URI de conexión directamente desde la variable de entorno
 * {@code SPRING_DATA_MONGODB_URI} mediante {@link System#getenv} para garantizar
 * que el valor se recoja independientemente de cómo Azure App Service resuelva
 * los marcadores de posición de las propiedades de Spring.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Configuration
@Profile("prod")
public class MongoConfig {

    /**
     * Crea el cliente MongoDB a partir del URI leído de la variable de entorno.
     *
     * @return cliente MongoDB configurado
     * @throws IllegalStateException si la variable de entorno no está definida o está vacía
     */
    @Bean
    public MongoClient mongoClient() {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                "Environment variable SPRING_DATA_MONGODB_URI is not set or is empty");
        }
        return MongoClients.create(uri);
    }

    /**
     * Crea la fábrica de base de datos MongoDB extrayendo el nombre de la base de datos del URI.
     *
     * @param mongoClient cliente MongoDB ya configurado
     * @return fábrica de base de datos MongoDB
     */
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        ConnectionString cs = new ConnectionString(uri);
        String database = cs.getDatabase() != null ? cs.getDatabase() : "test";
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    /**
     * Crea el template de operaciones MongoDB para uso en repositorios y servicios.
     *
     * @param mongoDatabaseFactory fábrica de base de datos MongoDB
     * @return template de MongoDB configurado
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}