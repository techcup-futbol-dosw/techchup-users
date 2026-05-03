package edu.dosw.users.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB client configuration for the production profile.
 *
 * <p>Reads the connection URI directly from the {@code SPRING_DATA_MONGODB_URI}
 * environment variable via {@link System#getenv} to ensure the value is picked
 * up regardless of how Azure App Service resolves Spring property placeholders.</p>
 */
@Configuration
@Profile("prod")
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException(
                "Environment variable SPRING_DATA_MONGODB_URI is not set or is empty");
        }
        return MongoClients.create(uri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        ConnectionString cs = new ConnectionString(uri);
        String database = cs.getDatabase() != null ? cs.getDatabase() : "test";
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }
}