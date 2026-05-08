package edu.dosw.users.client;

import edu.dosw.users.model.UserModel;

import java.util.List;

/**
 * Client interface for communicating with the identity microservice.
 *
 * <p>Used by the service layer to delegate all user-data operations (CRUD,
 * deactivation, inactivation, and search) to the identity service rather than
 * persisting user data locally.</p>
 */
public interface IdentityServiceClient {

    /**
     * Returns {@code true} when a user with the given identifier exists in the
     * identity service.
     *
     * @param id user identifier to check
     * @return {@code true} if the user exists
     */
    boolean userExists(Long id);

    /**
     * Retrieves a user by its identifier.
     *
     * @param id user identifier
     * @return the user model, or {@code null} if not found
     */
    UserModel getUserById(Long id);

    /**
     * Retrieves a user by their official identification number.
     *
     * @param identification official identification number
     * @return the user model, or {@code null} if not found
     */
    UserModel getUserByIdentification(String identification);

    /**
     * Returns all users registered in the identity service.
     *
     * @return list of all user models, may be empty
     */
    List<UserModel> getAllUsers();

    /**
     * Creates a new user in the identity service.
     *
     * @param model user data to create
     * @return the created user model with its generated identifier
     */
    UserModel createUser(UserModel model);

    /**
     * Replaces an existing user's data in the identity service.
     *
     * @param id    identifier of the user to update
     * @param model new user data
     * @return the updated user model
     */
    UserModel updateUser(Long id, UserModel model);

    /**
     * Updates the current user's own profile in the identity service.
     *
     * @param userId identifier of the authenticated user
     * @param model  new profile data (without credentials)
     * @return the updated user model
     */
    UserModel updateUserProfile(Long userId, UserModel model);

    /**
     * Sets a user's status to {@code INACTIVE} in the identity service.
     *
     * @param id identifier of the user to deactivate
     */
    void deactivateUser(Long id);

    /**
     * Inactivates a user in the identity service.
     *
     * @param id identifier of the user to inactivate
     */
    void inactivateUser(Long id);

    /**
     * Searches for users in the identity service matching the given filters.
     *
     * @param name   optional partial name filter (case-insensitive); {@code null} returns all
     * @param status optional status filter (e.g. {@code "ACTIVE"}); {@code null} returns all
     * @return list of matching user models, may be empty
     */
    List<UserModel> searchUsers(String name, String status);
}
