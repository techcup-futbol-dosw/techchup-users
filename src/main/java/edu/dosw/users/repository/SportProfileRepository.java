package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SportProfileRepository extends JpaRepository<SportProfileEntity, Long> {

    Optional<SportProfileEntity> findByUser_Id(Long userId);
}