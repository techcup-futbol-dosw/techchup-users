package edu.dosw.users.repository;

import edu.dosw.users.model.PlayerPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio Spring Data MongoDB para documentos {@link PlayerPhoto}.
 *
 * <p>Provee operaciones CRUD sobre la colección {@code player_photos} utilizando
 * el identificador de documento MongoDB como {@link String}.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see PlayerPhoto
 */
public interface PlayerPhotoRepository extends MongoRepository<PlayerPhoto, String> {
}
