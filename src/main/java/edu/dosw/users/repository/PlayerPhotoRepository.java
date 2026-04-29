package edu.dosw.users.repository;

import edu.dosw.users.model.PlayerPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for {@link PlayerPhoto} documents.
 *
 * <p>Provides CRUD operations over the {@code player_photos} collection using
 * the MongoDB document identifier as a {@link String}.</p>
 *
 * @see PlayerPhoto
 */
public interface PlayerPhotoRepository extends MongoRepository<PlayerPhoto, String> {
}
