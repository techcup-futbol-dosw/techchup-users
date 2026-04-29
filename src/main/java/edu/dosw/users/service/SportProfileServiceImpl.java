package edu.dosw.users.service;

import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Default implementation of {@link ISportProfileService}.
 *
 * <p>Coordinates sport profile persistence with photo storage (MongoDB via
 * {@link ImageService}), team membership checks (via {@link TeamsServiceClient}),
 * and audit logging (via {@link IAuditService}).</p>
 *
 * <p>Because {@link SportProfileMapper} ignores the {@code userProfile}
 * relationship, this class sets it manually using a partial entity reference
 * (id only) after mapping.</p>
 */
@Service
@RequiredArgsConstructor
public class SportProfileServiceImpl implements ISportProfileService {

    private static final String SPORT_PROFILE_NOT_FOUND_ID = "Sport profile not found with id: ";

    private final SportProfileRepository sportProfileRepository;
    private final UserRepository userRepository;
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
        return sportProfileRepository.findByUser_Id(userId)
                .map(sportProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sport profile not found for user id: " + userId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates the profile only when the user exists and does not already
     * have one. If a photo is provided, it is uploaded before the profile is
     * saved and the generated photo id is stored in the relational entity.</p>
     */
    @Override
    public SportProfileModel create(Long userId, SportProfileModel model, MultipartFile photo) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + userId));

        if (sportProfileRepository.findByUser_Id(userId).isPresent()) {
            throw new BusinessException(
                    "User with id " + userId + " already has a sport profile");
        }

        String photoId = uploadIfPresent(photo, null);

        LocalDateTime now = LocalDateTime.now();
        SportProfileEntity entity = sportProfileMapper.toEntity(model);
        entity.setUser(UserEntity.builder().id(userId).build());
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

        Long userId = existing.getUser().getId();
        if (teamsServiceClient.isPlayerAssignedToTeam(userId)) {
            throw new BusinessException(
                    "Cannot update sport profile while player is assigned to a team");
        }

        String photoId = uploadIfPresent(photo, existing.getPhotoId());

        SportProfileEntity updated = sportProfileMapper.toEntity(model);
        updated.setId(id);
        updated.setUser(existing.getUser());
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
