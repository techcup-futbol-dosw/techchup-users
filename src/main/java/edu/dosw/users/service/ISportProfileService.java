package edu.dosw.users.service;

import edu.dosw.users.model.SportProfileModel;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing player sport profiles.
 *
 * <p>A sport profile cannot be updated while the player is assigned to an
 * active team, and it can never be deleted. Photo management delegates to
 * {@link ImageService}.</p>
 */
public interface ISportProfileService {

    /**
     * Retrieves a sport profile by its identifier.
     *
     * @param id identifier of the sport profile
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    SportProfileModel getById(Long id);

    /**
     * Retrieves the sport profile associated with the given user.
     *
     * @param userId identifier of the user
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    SportProfileModel getByUserId(Long userId);

    /**
     * Creates a new sport profile for the specified user.
     *
     * @param userId identifier of the owner user
     * @param model  profile data
     * @param photo  optional player photo; may be {@code null} or empty
     * @return the saved model with its generated identifier
     * @throws edu.dosw.users.exception.ResourceNotFoundException if the user does not exist
     * @throws edu.dosw.users.exception.BusinessException         if the user already has a sport profile
     */
    SportProfileModel create(Long userId, SportProfileModel model, MultipartFile photo);

    /**
     * Updates an existing sport profile.
     *
     * @param id    identifier of the sport profile to update
     * @param model new profile data
     * @param photo optional new player photo; {@code null} keeps the existing photo
     * @return the updated model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     * @throws edu.dosw.users.exception.BusinessException         if the player is currently assigned to a team
     */
    SportProfileModel update(Long id, SportProfileModel model, MultipartFile photo);

    /**
     * Toggles the availability flag of the specified sport profile.
     *
     * @param id        identifier of the sport profile
     * @param available new availability value
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    void updateAvailability(Long id, boolean available);
}