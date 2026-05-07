package edu.dosw.users.service;

import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.UserRepository;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.SchoolRelation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation of {@link IUserService}.
 *
 * <p>Coordinates user profile persistence through {@link UserRepository} and
 * maps between persistence entities and domain models through {@link UserMapper}.</p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final String USER_NOT_FOUND_ID = "User not found with id: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
        private final IAuditService auditService;
        private final TeamsServiceClient teamsServiceClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getByIdentification(String identification) {
        return userRepository.findByIdentification(identification)
                .map(userMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with identification: " + identification));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserModel> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toModel)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Initializes the profile status and timestamps before persisting the
     * new user.</p>
     */
    @Override
    public UserModel create(UserModel model) {
        LocalDateTime now = LocalDateTime.now();
        model.setStatus("ACTIVE");
        model.setProfileCreatedAt(now);
        model.setUpdatedAt(now);
        return userMapper.toModel(
                userRepository.save(userMapper.toEntity(model)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifies that the user exists, preserves the requested identifier,
     * and refreshes the update timestamp before saving.</p>
     */
    @Override
    public UserModel update(Long id, UserModel model) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + id));

        if (!"ACTIVE".equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("Cannot update an inactive user.");
        }

        // Business rule: semester only allowed for STUDENT
        if (model.getSchoolRelation() != null
                && model.getSchoolRelation() != SchoolRelation.STUDENT
                && model.getSemester() != null) {
            throw new BusinessException("Semester can only be set for students.");
        }

        // Apply only the allowed fields to avoid overwriting protected values
        entity.setFullName(model.getFullName());
        entity.setSchoolRelation(model.getSchoolRelation() != null
                ? model.getSchoolRelation().name() : null);
        entity.setAcademicProgram(model.getAcademicProgram());
        entity.setSemester(model.getSemester());
        entity.setUpdatedAt(LocalDateTime.now());

        var saved = userRepository.save(entity);

        if (saved.getSportProfile() != null && saved.getSportProfile().getId() != null) {
            auditService.logSportProfile(saved.getSportProfile().getId(), AuditAction.UPDATE,
                    "Admin updated user with id: " + id);
        }

        return userMapper.toModel(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel updateProfile(Long userId, UserModel model) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + userId));
        UserEntity updated = userMapper.toEntity(model);
        entity.setFullName(updated.getFullName());
        entity.setIdentification(updated.getIdentification());
        entity.setBirthDate(updated.getBirthDate());
        entity.setGender(updated.getGender());
        entity.setSchoolRelation(updated.getSchoolRelation());
        entity.setAcademicProgram(updated.getAcademicProgram());
        entity.setSemester(updated.getSemester());
        entity.setUpdatedAt(LocalDateTime.now());
        return userMapper.toModel(userRepository.save(entity));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a logical deactivation by changing the status instead of
     * deleting the row.</p>
     */
    @Override
    public void deactivate(Long id) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + id));
        entity.setStatus("INACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserModel> search(String name, String position, String status) {
        String nameParam = (name == null || name.isBlank()) ? null : name.trim();
        String statusParam = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        String positionParam = (position == null || position.isBlank()) ? null : position.trim().toUpperCase();
        return userRepository.searchPlayers(nameParam, statusParam, positionParam)
                .stream()
                .map(userMapper::toModel)
                .toList();
    }

    @Override
    public void inactivate(Long id) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + id));
        if (!"ACTIVE".equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("La cuenta ya se encuentra inactiva");
        }
        if (teamsServiceClient.isPlayerAssignedToTeam(id)) {
            throw new BusinessException(
                    "No es posible inactivar la cuenta mientras el usuario participa en un torneo activo");
        }
        entity.setStatus("INACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(entity);
    }
}
