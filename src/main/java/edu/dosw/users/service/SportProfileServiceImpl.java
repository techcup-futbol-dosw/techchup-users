package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.repository.SportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Default implementation of {@link ISportProfileService}.
 *
 * <p>Coordinates sport profile persistence with photo storage (MongoDB via
 * {@link ImageService}), team membership checks (via {@link TeamsServiceClient}),
 * user existence validation (via {@link IdentityServiceClient}),
 * and audit logging (via {@link IAuditService}).</p>
 */
@Service
@RequiredArgsConstructor
public class SportProfileServiceImpl implements ISportProfileService {

    private static final String SPORT_PROFILE_NOT_FOUND_ID = "Sport profile not found with id: ";

    private final SportProfileRepository sportProfileRepository;
    private final IdentityServiceClient identityServiceClient;
    private final SportProfileMapper sportProfileMapper;
    private final IAuditService auditService;
    private final ImageService imageService;
    private final TeamsServiceClient teamsServiceClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public SportProfileModel getById(Long id) {
        return sportProfileRepository.findById(id)
                .map(sportProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SPORT_PROFILE_NOT_FOUND_ID + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SportProfileModel getByUserId(Long userId) {
        return sportProfileRepository.findByUserId(userId)
                .map(sportProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sport profile not found for user id: " + userId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates the profile only when the user exists in the identity service
     * and does not already have one. If a photo is provided, it is uploaded
     * before the profile is saved and the generated photo id is stored in the
     * relational entity.</p>
     */
    @Override
    public SportProfileModel create(Long userId, SportProfileModel model, MultipartFile photo) {
        if (!identityServiceClient.userExists(userId)) {
            throw new ResourceNotFoundException(
                    "User profile not found with id: " + userId);
        }

        if (sportProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(
                    "User with id " + userId + " already has a sport profile");
        }

        String photoId = uploadIfPresent(photo, null);

        LocalDateTime now = LocalDateTime.now();
        SportProfileEntity entity = sportProfileMapper.toEntity(model);
        entity.setUserId(userId);
        entity.setPhotoId(photoId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        SportProfileModel saved = sportProfileMapper.toModel(sportProfileRepository.save(entity));
        auditService.logSportProfile(saved.getId(), AuditAction.CREATE,
                "Sport profile created for user " + userId);
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Prevents updates while the player is assigned to a team. When a new
     * photo is provided, the previous stored photo is deleted and replaced.</p>
     */
    @Override
    public SportProfileModel update(Long id, SportProfileModel model, MultipartFile photo) {
        SportProfileEntity existing = sportProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SPORT_PROFILE_NOT_FOUND_ID + id));

        Long userId = existing.getUserId();
        if (teamsServiceClient.isPlayerAssignedToTeam(userId)) {
            throw new BusinessException(
                    "Cannot update sport profile while player is assigned to a team");
        }

        String photoId = uploadIfPresent(photo, existing.getPhotoId());

        SportProfileEntity updated = sportProfileMapper.toEntity(model);
        updated.setId(id);
        updated.setUserId(userId);
        updated.setPhotoId(photoId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());

        SportProfileModel saved = sportProfileMapper.toModel(sportProfileRepository.save(updated));
        auditService.logSportProfile(id, AuditAction.UPDATE,
                "Sport profile updated for user " + userId);
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Updates only the availability flag and the modification timestamp.</p>
     */
    @Override
    public void updateAvailability(Long id, boolean available) {
        SportProfileEntity entity = sportProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SPORT_PROFILE_NOT_FOUND_ID + id));
        entity.setAvailable(available);
        entity.setUpdatedAt(LocalDateTime.now());
        sportProfileRepository.save(entity);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Uploads a new photo and deletes the previous one when a new file is
     * provided. Returns the current {@code photoId} unchanged when no file
     * is given.
     *
     * @param photo optional multipart file to upload
     * @param currentPhotoId current stored photo identifier
     * @return new photo identifier, or the current one when no new file exists
     */
    private String uploadIfPresent(MultipartFile photo, String currentPhotoId) {
        if (photo == null || photo.isEmpty()) {
            return currentPhotoId;
        }
        if (currentPhotoId != null) {
            imageService.delete(currentPhotoId);
        }
        return imageService.upload(photo, null);
    }
}
