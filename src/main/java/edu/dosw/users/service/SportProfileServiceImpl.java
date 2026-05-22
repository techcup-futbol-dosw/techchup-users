package edu.dosw.users.service;

import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.repository.UserRepository;
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
 * Implementación por defecto de {@link ISportProfileService}.
 *
 * <p>Coordina la persistencia del perfil deportivo con el almacenamiento de fotos
 * (MongoDB a través de {@link ImageService}), la verificación de pertenencia a equipos
 * (vía {@link TeamsServiceClient}), la validación de existencia de usuarios
 * (vía {@link IdentityServiceClient}) y el registro de auditoría (vía {@link IAuditService}).</p>
 *
 * @author CodeForge
 * @since 1.0
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
        return sportProfileRepository.findByUserId(userId)
                .map(sportProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sport profile not found for user id: " + userId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Crea el perfil solo cuando el usuario existe en el servicio de identidad
     * y aún no tiene uno. Si se proporciona una foto, se sube antes de guardar el
     * perfil y el id de foto generado se almacena en la entidad relacional.</p>
     */
    @Override
    public SportProfileModel create(Long userId, SportProfileModel model, MultipartFile photo) {
        if (!userRepository.existsById(userId)) {
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
     * <p>Impide las actualizaciones mientras el jugador esté asignado a un equipo.
     * Cuando se proporciona una nueva foto, la foto almacenada anteriormente se
     * elimina y se reemplaza.</p>
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
     * <p>Actualiza únicamente el indicador de disponibilidad y la marca de tiempo
     * de modificación.</p>
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
     * Sube una nueva foto y elimina la anterior cuando se proporciona un nuevo archivo.
     * Retorna el {@code photoId} actual sin cambios cuando no se proporciona ningún archivo.
     *
     * @param photo          archivo multipart a subir (opcional)
     * @param currentPhotoId identificador de la foto actualmente almacenada
     * @return nuevo identificador de foto, o el identificador actual si no hay nuevo archivo
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
