package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.dto.AccountDto;
import edu.dosw.users.dto.PlayerSearchResponse;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final String USER_NOT_FOUND_ID = "User not found with id: ";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TeamsServiceClient teamsServiceClient;
    private final SportProfileRepository sportProfileRepository;
    private final IAuditService auditService;
    private final IdentityServiceClient identityServiceClient;

    // ── Lectura ──────────────────────────────────────────────────────────────

    /**
     * Busca el usuario en la base de datos local; si no existe, lo solicita al
     * Identity Service vía API Gateway (read-through). No persiste el resultado
     * de Identity para no generar registros incompletos.
     */
    @Override
    public UserModel getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toModel)
                .orElseGet(() -> {
                    AccountDto account = identityServiceClient.getAccountById(id);
                    if (account == null) {
                        throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
                    }
                    return mapAccountToModel(account);
                });
    }

    /**
     * Busca solo en la base de datos local. Los usuarios aparecen aquí una vez que
     * han completado su perfil mediante {@link #updateProfile}.
     */
    @Override
    public UserModel getByIdentification(String identification) {
        return userRepository.findByIdentification(identification)
                .map(userMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found with identification: " + identification));
    }

    /**
     * Retorna los usuarios presentes en la base de datos local.
     */
    @Override
    public List<UserModel> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toModel)
                .toList();
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserModel update(Long id, UserModel model) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ID + id));

        if (!STATUS_ACTIVE.equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("Cannot update an inactive user.");
        }

        if (model.getSchoolRelation() != null
                && model.getSchoolRelation() != SchoolRelation.STUDENT
                && model.getSemester() != null) {
            throw new BusinessException("Semester can only be set for students.");
        }

        applyAdminUpdate(entity, model);
        entity.setUpdatedAt(LocalDateTime.now());
        UserModel updated = userMapper.toModel(userRepository.save(entity));

        sportProfileRepository.findByUserId(id).ifPresent(sp ->
                auditService.logSportProfile(sp.getId(), AuditAction.UPDATE,
                        "Admin updated user with id: " + id));

        return updated;
    }

    /**
     * Actualiza el perfil del usuario. Si no existe en la base de datos local,
     * realiza un upsert: obtiene los datos base del Identity Service vía API
     * Gateway, crea el registro local y luego aplica los cambios del request.
     */
    @Override
    @Transactional
    public UserModel updateProfile(Long userId, UserModel model) {
        UserEntity entity = userRepository.findById(userId)
                .orElseGet(() -> {
                    AccountDto account = identityServiceClient.getAccountById(userId);
                    if (account == null) {
                        throw new ResourceNotFoundException(USER_NOT_FOUND_ID + userId);
                    }
                    return mapAccountToEntity(account);
                });

        applyProfileUpdate(entity, model);
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getProfileCreatedAt() == null) {
            entity.setProfileCreatedAt(LocalDateTime.now());
        }
        return userMapper.toModel(userRepository.save(entity));
    }

    /**
     * Marca la cuenta como INACTIVE. Si el usuario no existe en la base de datos
     * local (registrado solo en Identity Service), crea un registro mínimo para
     * poder almacenar el estado. Esto garantiza que las llamadas de sincronización
     * entrantes desde el Identity Service nunca fallen con 404.
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseGet(() -> UserEntity.builder()
                        .id(id)
                        .status(STATUS_ACTIVE)
                        .profileCreatedAt(LocalDateTime.now())
                        .build());
        entity.setStatus("INACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(entity);
    }

    /**
     * Inactiva la cuenta del usuario tras validar que no pertenece a un equipo
     * activo. Realiza upsert desde Identity Service si el usuario no existe
     * localmente.
     */
    @Override
    @Transactional
    public void inactivate(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseGet(() -> {
                    AccountDto account = identityServiceClient.getAccountById(id);
                    if (account == null) {
                        throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
                    }
                    return mapAccountToEntity(account);
                });

        if (!STATUS_ACTIVE.equalsIgnoreCase(entity.getStatus())) {
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

    /**
     * Reactiva la cuenta del usuario. Realiza upsert desde Identity Service si el
     * usuario no existe localmente.
     */
    @Override
    @Transactional
    public void reactivate(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseGet(() -> {
                    AccountDto account = identityServiceClient.getAccountById(id);
                    if (account == null) {
                        throw new ResourceNotFoundException(USER_NOT_FOUND_ID + id);
                    }
                    return mapAccountToEntity(account);
                });

        if (STATUS_ACTIVE.equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("La cuenta ya se encuentra activa");
        }
        entity.setStatus("ACTIVE");
        entity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(entity);
    }

    // ── Búsqueda ─────────────────────────────────────────────────────────────

    @Override
    public List<UserModel> search(String name, String position, String status,
                                  String identification, String gender,
                                  Integer semester, Integer age, Boolean onlyAvailable) {
        String nameParam     = blank(name)     ? null : name.trim();
        String statusParam   = blank(status)   ? null : status.trim().toUpperCase();
        String positionParam = blank(position) ? null : position.trim().toUpperCase();
        String genderParam   = blank(gender)   ? null : gender.trim().toUpperCase();

        List<UserEntity> entities = userRepository.findAll().stream()
                .filter(u -> nameParam == null
                        || (u.getFullName() != null
                            && u.getFullName().toLowerCase().contains(nameParam.toLowerCase())))
                .filter(u -> statusParam == null || statusParam.equalsIgnoreCase(u.getStatus()))
                .collect(Collectors.toList());

        // ── Filtros sobre el perfil deportivo ─────────────────────────────────
        boolean filterPosition  = positionParam != null;
        boolean filterAvailable = Boolean.TRUE.equals(onlyAvailable);

        if (filterPosition || filterAvailable) {
            Set<Long> ids = getSportProfileUserIds(positionParam, filterPosition, filterAvailable);
            entities = entities.stream().filter(u -> ids.contains(u.getId())).toList();
        }

        // ── Filtros locales adicionales ────────────────────────────────────────
        if (identification != null && !identification.isBlank()) {
            String id = identification.trim();
            entities = entities.stream().filter(u -> id.equals(u.getIdentification())).toList();
        }
        if (genderParam != null) {
            String gp = genderParam;
            entities = entities.stream()
                    .filter(u -> u.getGender() != null && gp.equals(u.getGender().name()))
                    .toList();
        }
        if (semester != null) {
            entities = entities.stream().filter(u -> semester.equals(u.getSemester())).toList();
        }
        if (age != null) {
            entities = entities.stream()
                    .filter(u -> age == computeAge(u.getBirthDate()))
                    .toList();
        }

        return entities.stream().map(userMapper::toModel).toList();
    }

    @Override
    public List<PlayerSearchResponse> searchPlayers(String name, String position, String status,
                                                     String identification, String gender,
                                                     Integer semester, Integer age, Boolean onlyAvailable) {
        List<UserModel> users = search(name, position, status, identification, gender, semester, age, onlyAvailable);

        if (users.isEmpty()) {
            return List.of();
        }

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

    // ── Mapeo desde Identity Service ─────────────────────────────────────────

    private UserModel mapAccountToModel(AccountDto account) {
        return UserModel.builder()
                .id(account.getId())
                .fullName(resolveFullName(account))
                .email(account.getEmail())
                .identification(account.getIdentification())
                .birthDate(account.getBirthDate())
                .gender(mapGender(account.getGender()))
                .schoolRelation(mapRelation(account.getRelation()))
                .semester(account.getSemester())
                .academicProgram(account.getProgram())
                .status(account.getStatus() != null ? account.getStatus() : STATUS_ACTIVE)
                .profileCreatedAt(account.getCreatedAt())
                .build();
    }

    private UserEntity mapAccountToEntity(AccountDto account) {
        return UserEntity.builder()
                .id(account.getId())
                .fullName(resolveFullName(account))
                .email(account.getEmail())
                .identification(account.getIdentification())
                .birthDate(account.getBirthDate())
                .gender(mapGender(account.getGender()))
                .schoolRelation(mapRelation(account.getRelation()))
                .semester(account.getSemester())
                .academicProgram(account.getProgram())
                .status(account.getStatus() != null ? account.getStatus() : STATUS_ACTIVE)
                .profileCreatedAt(account.getCreatedAt() != null
                        ? account.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private String resolveFullName(AccountDto account) {
        if (account.getFullName() != null && !account.getFullName().isBlank()) {
            return account.getFullName();
        }
        String name = account.getName();
        String lastName = account.getLastName();
        if (name == null && lastName == null) return null;
        if (name == null) return lastName;
        if (lastName == null) return name;
        return name + " " + lastName;
    }

    private Gender mapGender(String gender) {
        if (gender == null) return null;
        try {
            return Gender.valueOf(gender);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SchoolRelation mapRelation(String relation) {
        if (relation == null) return null;
        return switch (relation) {
            case "ESTUDIANTE"    -> SchoolRelation.STUDENT;
            case "GRADUADO"      -> SchoolRelation.GRADUATE;
            case "PROFESOR"      -> SchoolRelation.PROFESSOR;
            case "PERSONAL_ADMIN"-> SchoolRelation.ADMINISTRATIVE;
            case "FAMILIAR"      -> SchoolRelation.FAMILY;
            default              -> null;
        };
    }

    // ── Helpers generales ─────────────────────────────────────────────────────

    private void applyAdminUpdate(UserEntity entity, UserModel model) {
        if (model.getFullName() != null) entity.setFullName(model.getFullName());
        if (model.getSchoolRelation() != null) entity.setSchoolRelation(model.getSchoolRelation());
        if (model.getAcademicProgram() != null) entity.setAcademicProgram(model.getAcademicProgram());
        if (model.getSemester() != null) entity.setSemester(model.getSemester());
    }

    private void applyProfileUpdate(UserEntity entity, UserModel model) {
        if (model.getFullName() != null) entity.setFullName(model.getFullName());
        if (model.getIdentification() != null) entity.setIdentification(model.getIdentification());
        if (model.getBirthDate() != null) entity.setBirthDate(model.getBirthDate());
        if (model.getGender() != null) entity.setGender(model.getGender());
        if (model.getSchoolRelation() != null) entity.setSchoolRelation(model.getSchoolRelation());
        if (model.getAcademicProgram() != null) entity.setAcademicProgram(model.getAcademicProgram());
        if (model.getSemester() != null) entity.setSemester(model.getSemester());
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

    private int computeAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
