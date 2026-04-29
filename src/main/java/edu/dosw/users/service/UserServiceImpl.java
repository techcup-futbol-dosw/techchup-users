package edu.dosw.users.service;

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
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserModel getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
    }

    @Override
    public UserModel getByIdentification(String identification) {
        return userRepository.findByIdentification(identification)
                .map(userMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with identification: " + identification));
    }

    @Override
    public List<UserModel> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toModel)
                .toList();
    }

    @Override
    public UserModel create(UserModel model) {
        LocalDateTime now = LocalDateTime.now();
        model.setStatus("ACTIVE");
        model.setProfileCreatedAt(now);
        model.setUpdatedAt(now);
        return userMapper.toModel(
                userRepository.save(userMapper.toEntity(model)));
    }

    @Override
    public UserModel update(Long id, UserModel model) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
        model.setId(id);
        model.setUpdatedAt(LocalDateTime.now());
        return userMapper.toModel(
                userRepository.save(userMapper.toEntity(model)));
    }

    @Override
    public void deactivate(Long id) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with id: " + id));
        entity.setStatus("INACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(entity);
    }
}