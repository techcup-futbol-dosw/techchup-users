package edu.dosw.users.service;

import edu.dosw.users.model.UserProfileModel;

import java.util.List;

/**
 * Service for managing user profiles.
 *
 * <p>Handles CRUD operations on {@link UserProfileModel}. Deactivation sets
 * the profile status to {@code "INACTIVE"} without removing data from the
 * database.</p>
 */
public interface IUserProfileService {

    /**
     * Retrieves a user profile by its identifier.
     *
     * @param id identifier of the profile
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserProfileModel getById(Long id);

    /**
     * Retrieves a user profile by the user's official identification number.
     *
     * @param identification official ID number
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserProfileModel getByIdentification(String identification);

    /**
     * Returns all user profiles stored in the system.
     *
     * @return list of all models, may be empty
     */
    List<UserProfileModel> getAll();

    /**
     * Persists a new user profile. Sets {@code profileCreatedAt}, {@code updatedAt}
     * and {@code status} to {@code "ACTIVE"} automatically.
     *
     * @param model profile data to create
     * @return the saved model with its generated identifier
     */
    UserProfileModel create(UserProfileModel model);

    /**
     * Updates an existing user profile with the data provided.
     *
     * @param id    identifier of the profile to update
     * @param model new profile data
     * @return the updated model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserProfileModel update(Long id, UserProfileModel model);

    /**
     * Deactivates a user profile by setting its status to {@code "INACTIVE"}.
     *
     * @param id identifier of the profile to deactivate
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    void deactivate(Long id);
}