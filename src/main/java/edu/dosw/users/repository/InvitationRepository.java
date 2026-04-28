package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    List<InvitationEntity> findByPlayer_Id(Long playerId);

    List<InvitationEntity> findByPlayer_IdAndStatus(Long playerId, String status);
}