package edu.dosw.users.service;

import edu.dosw.users.model.UserModel;

import java.util.List;

/**
 * Service for managing user profiles.
 *
 * <p>Handles CRUD operations on {@link UserModel}. Deactivation sets
 * the profile status to {@code "INACTIVE"} without removing data from the
 * database.</p>
 */
public interface IUserService {

    /**
     * Retrieves a user profile by its identifier.
     *
     * @param id identifier of the profile
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserModel getById(Long id);

    /**
     * Retrieves a user profile by the user's official identification number.
     *
     * @param identification official ID number
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserModel getByIdentification(String identification);

    /**
     * Returns all user profiles stored in the system.
     *
     * @return list of all models, may be empty
     */
    List<UserModel> getAll();

    /**
     * Updates an existing user profile with the data provided.
     *
     * @param id    identifier of the profile to update
     * @param model new profile data
     * @return the updated model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserModel update(Long id, UserModel model);

    /**
     * Updates the current user's profile without changing email or password.
     *
     * @param userId identifier of the current user
     * @param model  new profile data
     * @return the updated profile model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    UserModel updateProfile(Long userId, UserModel model);

    /**
     * Deactivates a user profile by setting its status to {@code "INACTIVE"}.
     *
     * @param id identifier of the profile to deactivate
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    void deactivate(Long id);

    /**
     * Inactivates a user profile after validating team participation.
     *
     * @param id identifier of the profile to inactivate
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     * @throws edu.dosw.users.exception.BusinessException when business rules fail
     */
    void inactivate(Long id);

    /**
     * Searches for players matching the given filters.
     *
     * <p>Name and status are delegated to the identity service; the remaining
     * filters are applied locally using sport-profile and user-model data.</p>
     *
     * @param name           optional partial name filter (case-insensitive)
     * @param position       optional position filter (e.g. {@code "FORWARD"})
     * @param status         optional status filter (e.g. {@code "ACTIVE"})
     * @param identification optional exact identification number filter
     * @param gender         optional gender filter
     * @param semester       optional exact semester filter
     * @param age            optional exact age filter (calculated from birthDate)
     * @param onlyAvailable  when {@code true}, only returns players whose sport profile is available
     * @return list of matching user models, may be empty
     */
    List<UserModel> search(String name, String position, String status,
                           String identification, String gender,
                           Integer semester, Integer age, Boolean onlyAvailable);
}