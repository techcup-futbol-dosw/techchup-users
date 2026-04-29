package edu.dosw.users.service;

import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.UserRepository;
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
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_ID + id));
        model.setId(id);
        model.setUpdatedAt(LocalDateTime.now());
        return userMapper.toModel(
                userRepository.save(userMapper.toEntity(model)));
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
}
