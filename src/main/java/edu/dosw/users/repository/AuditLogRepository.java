package edu.dosw.users.repository;

import edu.dosw.users.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findBySportProfile_Id(Long sportProfileId);

    List<AuditLogEntity> findByInvitation_Id(Long invitationId);
}