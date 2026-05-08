package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.SportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IUserService} that delegates all user-data
 * operations to the identity microservice via {@link IdentityServiceClient}.
 *
 * <p>Business rules that belong to this service (team participation checks,
 * sport-profile audit logging, position-based search filtering) are applied
 * locally before or after the identity-service call.</p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final String USER_NOT_FOUND_ID = "User not found with id: ";

    private final IdentityServiceClient identityServiceClient;
    private final TeamsServiceClient teamsServiceClient;
    private final SportProfileRepository sportProfileRepository;
    private final IAuditService auditService;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getById(Long id) {
        UserModel user = identityServiceClient.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getByIdentification(String identification) {
        UserModel user = identityServiceClient.getUserByIdentification(identification);
        if (user == null) {
            throw new ResourceNotFoundException(
                    "User profile not found with identification: " + identification);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserModel> getAll() {
        return identityServiceClient.getAllUsers();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Pre-populates {@code status}, {@code profileCreatedAt} and
     * {@code updatedAt} before delegating to the identity service.</p>
     */
    @Override
    public UserModel create(UserModel model) {
        LocalDateTime now = LocalDateTime.now();
        model.setStatus("ACTIVE");
        model.setProfileCreatedAt(now);
        model.setUpdatedAt(now);
        return identityServiceClient.createUser(model);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enforces that the user is active and applies the semester/school-relation
     * constraint before delegating the update to the identity service. If the
     * user has a sport profile, an audit entry is recorded.</p>
     */
    @Override
    public UserModel update(Long id, UserModel model) {
        UserModel existing = identityServiceClient.getUserById(id);
        if (existing == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
        }

        if (!"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException("Cannot update an inactive user.");
        }

        if (model.getSchoolRelation() != null
                && model.getSchoolRelation() != SchoolRelation.STUDENT
                && model.getSemester() != null) {
            throw new BusinessException("Semester can only be set for students.");
        }

        UserModel updated = identityServiceClient.updateUser(id, model);

        sportProfileRepository.findByUserId(id).ifPresent(sp ->
                auditService.logSportProfile(sp.getId(), AuditAction.UPDATE,
                        "Admin updated user with id: " + id));

        return updated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel updateProfile(Long userId, UserModel model) {
        UserModel existing = identityServiceClient.getUserById(userId);
        if (existing == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_ID + userId);
        }
        return identityServiceClient.updateUserProfile(userId, model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate(Long id) {
        UserModel user = identityServiceClient.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
        }
        identityServiceClient.deactivateUser(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates the user's current status and team participation before
     * delegating the inactivation to the identity service.</p>
     */
    @Override
    public void inactivate(Long id) {
        UserModel user = identityServiceClient.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("La cuenta ya se encuentra inactiva");
        }
        if (teamsServiceClient.isPlayerAssignedToTeam(id)) {
            throw new BusinessException(
                    "No es posible inactivar la cuenta mientras el usuario participa en un torneo activo");
        }
        identityServiceClient.inactivateUser(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches users from the identity service filtered by name and status,
     * then applies a local position filter using sport-profile data when
     * {@code position} is provided.</p>
     */
    @Override
    public List<UserModel> search(String name, String position, String status) {
        String nameParam = (name == null || name.isBlank()) ? null : name.trim();
        String statusParam = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        String positionParam = (position == null || position.isBlank()) ? null : position.trim().toUpperCase();

        List<UserModel> users = identityServiceClient.searchUsers(nameParam, statusParam);

        if (positionParam != null) {
            String pos = positionParam;
            Set<Long> userIdsWithPosition = sportProfileRepository.findByPosition(pos)
                    .stream()
                    .map(sp -> sp.getUserId())
                    .collect(Collectors.toSet());
            users = users.stream()
                    .filter(u -> userIdsWithPosition.contains(u.getId()))
                    .toList();
        }

        return users;
    }
}
