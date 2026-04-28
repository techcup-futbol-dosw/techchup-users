package edu.dosw.users.service;

import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserProfileMapper;
import edu.dosw.users.model.UserProfileModel;
import edu.dosw.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation of {@link IUserProfileService}.
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileModel getById(Long id) {
        return userProfileRepository.findById(id)
                .map(userProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
    }

    @Override
    public UserProfileModel getByIdentification(String identification) {
        return userProfileRepository.findByIdentification(identification)
                .map(userProfileMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with identification: " + identification));
    }

    @Override
    public List<UserProfileModel> getAll() {
        return userProfileRepository.findAll()
                .stream()
                .map(userProfileMapper::toModel)
                .toList();
    }

    @Override
    public UserProfileModel create(UserProfileModel model) {
        LocalDateTime now = LocalDateTime.now();
        model.setStatus("ACTIVE");
        model.setProfileCreatedAt(now);
        model.setUpdatedAt(now);
        return userProfileMapper.toModel(
                userProfileRepository.save(userProfileMapper.toEntity(model)));
    }

    @Override
    public UserProfileModel update(Long id, UserProfileModel model) {
        userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
        model.setId(id);
        model.setUpdatedAt(LocalDateTime.now());
        return userProfileMapper.toModel(
                userProfileRepository.save(userProfileMapper.toEntity(model)));
    }

    @Override
    public void deactivate(Long id) {
        var entity = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
        entity.setStatus("INACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(entity);
    }
}