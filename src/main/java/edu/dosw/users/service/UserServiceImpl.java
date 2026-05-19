package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.dto.PlayerSearchResponse;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.SportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private static final String STATUS_ACTIVE = "ACTIVE";

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

        if (!STATUS_ACTIVE.equalsIgnoreCase(existing.getStatus())) {
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
        if (!STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
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
     * then applies local filters for position, availability, identification,
     * gender, semester and age.</p>
     */
    @Override
    public List<UserModel> search(String name, String position, String status,
                                  String identification, String gender,
                                  Integer semester, Integer age, Boolean onlyAvailable) {
        String nameParam       = blank(name)     ? null : name.trim();
        String statusParam     = blank(status)   ? null : status.trim().toUpperCase();
        String positionParam   = blank(position) ? null : position.trim().toUpperCase();
        String genderParam     = blank(gender)   ? null : gender.trim().toUpperCase();

        List<UserModel> users = identityServiceClient.searchUsers(nameParam, statusParam);

        // ── Sport-profile filters (position + availability) ───────────────────
        boolean filterPosition  = positionParam != null;
        boolean filterAvailable = Boolean.TRUE.equals(onlyAvailable);

        if (filterPosition || filterAvailable) {
            Set<Long> ids = getSportProfileUserIds(positionParam, filterPosition, filterAvailable);
            users = users.stream().filter(u -> ids.contains(u.getId())).toList();
        }

        // ── Identity-model local filters ──────────────────────────────────────
        if (identification != null && !identification.isBlank()) {
            String id = identification.trim();
            users = users.stream().filter(u -> id.equals(u.getIdentification())).toList();
        }
        if (genderParam != null) {
            users = users.stream()
                    .filter(u -> u.getGender() != null && genderParam.equals(u.getGender().name()))
                    .toList();
        }
        if (semester != null) {
            users = users.stream().filter(u -> semester.equals(u.getSemester())).toList();
        }
        if (age != null) {
            users = users.stream().filter(u -> age == u.getAge()).toList();
        }

        return users;
    }

    private Set<Long> getSportProfileUserIds(String position, boolean filterPosition, boolean filterAvailable) {
        List<SportProfileEntity> profiles;
        if (filterPosition && filterAvailable) {
            profiles = sportProfileRepository.findByPositionAndAvailable(position, true);
        } else if (filterPosition) {
            profiles = sportProfileRepository.findByPosition(position);
        } else {
            profiles = sportProfileRepository.findByAvailable(true);
        }
        return profiles.stream().map(SportProfileEntity::getUserId).collect(Collectors.toSet());
    }

    @Override
    public List<PlayerSearchResponse> searchPlayers(String name, String position, String status,
                                                     String identification, String gender,
                                                     Integer semester, Integer age, Boolean onlyAvailable) {
        List<UserModel> users = search(name, position, status, identification, gender, semester, age, onlyAvailable);

        if (users.isEmpty()) {
            return List.of();
        }

        // Batch-load sport profiles and index by userId for O(1) lookup
        Set<Long> userIds = users.stream().map(UserModel::getId).collect(Collectors.toSet());
        Map<Long, SportProfileEntity> profileByUserId = sportProfileRepository.findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(SportProfileEntity::getUserId, sp -> sp));

        return users.stream().map(u -> {
            SportProfileEntity sp = profileByUserId.get(u.getId());
            return PlayerSearchResponse.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .identification(u.getIdentification())
                    .birthDate(u.getBirthDate())
                    .gender(u.getGender())
                    .schoolRelation(u.getSchoolRelation())
                    .academicProgram(u.getAcademicProgram())
                    .semester(u.getSemester())
                    .status(u.getStatus())
                    .profileCreatedAt(u.getProfileCreatedAt())
                    .updatedAt(u.getUpdatedAt())
                    .position(sp != null ? sp.getPosition() : null)
                    .dorsalNumber(sp != null ? sp.getDorsalNumber() : null)
                    .available(sp != null ? sp.isAvailable() : null)
                    .build();
        }).toList();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
