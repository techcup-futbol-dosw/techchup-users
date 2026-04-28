package edu.dosw.users.repository;

import edu.dosw.users.model.PlayerPhoto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerPhotoRepository extends MongoRepository<PlayerPhoto, String> {
}