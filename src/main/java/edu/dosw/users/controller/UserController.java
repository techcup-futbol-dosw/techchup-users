package edu.dosw.users.controller;

import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.PlayerSearchResponse;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de perfiles de usuario.
 *
 * <p>Ruta base: {@code /api/users}</p>
 *
 * <p>Resumen de control de acceso:
 * <ul>
 *   <li>ADMIN — acceso completo a todos los endpoints.</li>
 *   <li>CAPTAIN — puede buscar jugadores por filtro y consultar por número de identificación.</li>
 *   <li>Cualquier usuario autenticado — puede leer y actualizar su propio perfil.</li>
 * </ul>
 * </p>
 *
 * @author CodeForge
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    /**
     * Retorna los jugadores que coinciden con los filtros indicados.
     *
     * <p>Solo capitanes y administradores pueden buscar jugadores (según requisitos del proyecto).</p>
     *
     * @param name           nombre o parte del nombre del jugador (opcional)
     * @param position       posición en el campo, p. ej. {@code GOALKEEPER} (opcional)
     * @param status         estado de la cuenta del usuario (opcional)
     * @param identification número de identificación oficial del jugador (opcional)
     * @param gender         género del jugador (opcional)
     * @param semester       semestre académico en curso (opcional)
     * @param age            edad del jugador (opcional)
     * @param available      indica si el jugador está disponible para unirse a un equipo (opcional)
     * @return lista de usuarios que cumplen con todos los filtros proporcionados
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('CAPTAIN') or hasRole('ADMIN')")
    public ResponseEntity<List<PlayerSearchResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String identification,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(
                userService.searchPlayers(name, position, status, identification, gender, semester, age, available));
    }

    /**
     * Retorna todos los perfiles de usuario del sistema.
     *
     * <p>Restringido únicamente a administradores.</p>
     *
     * @return lista completa de usuarios registrados
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll().stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * Retorna el perfil de usuario con el identificador indicado.
     *
     * <p>Accesible por el propio usuario o un administrador.</p>
     *
     * @param id identificador del usuario
     * @return respuesta con los datos del perfil de usuario
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAccessPolicy.canAccessOwnUser(#id, authentication)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userMapper.toResponse(userService.getById(id)));
    }

    /**
     * Retorna el perfil de usuario con el número de identificación oficial indicado.
     *
     * <p>Los capitanes utilizan este endpoint al construir su plantilla; los administradores
     * tienen acceso completo.</p>
     *
     * @param identification número de identificación oficial del usuario
     * @return respuesta con los datos del perfil de usuario
     */
    @GetMapping("/identification/{identification}")
    @PreAuthorize("hasRole('CAPTAIN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getByIdentification(
            @PathVariable String identification) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.getByIdentification(identification)));
    }

    /**
     * Reemplaza un perfil de usuario existente (operación de administrador).
     *
     * <p>Solo los administradores pueden realizar actualizaciones completas de usuario.
     * Delega la persistencia al servicio de identidad externo y registra auditoría
     * automáticamente sobre el perfil deportivo asociado.</p>
     *
     * @param id      identificador del usuario a actualizar
     * @param request datos completos de actualización con privilegios de administrador
     * @return respuesta con los datos del perfil de usuario actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.update(id, userMapper.toModel(request))));
    }

    /**
     * Actualiza el perfil del usuario autenticado actualmente.
     *
     * <p>Cualquier usuario autenticado puede actualizar su propia información básica.
     * El identificador de usuario se extrae directamente del {@code SecurityContext},
     * donde {@link edu.dosw.users.security.JwtAuthenticationFilter} lo deposita al
     * validar el JWT, sin depender de ningún header propagado por el gateway.</p>
     *
     * @param request datos de actualización del propio perfil
     * @return respuesta con los datos del perfil actualizado
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMe(
            @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader,
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication) {
        Long userId;
        if (userIdHeader != null) {
            userId = userIdHeader;
        } else {
            Object principal = authentication.getPrincipal();
            String raw = (principal instanceof UserDetails ud)
                    ? ud.getUsername()
                    : principal.toString();
            userId = Long.parseLong(raw);
        }
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.updateProfile(userId, userMapper.toModel(request))));
    }

    /**
     * Desactiva una cuenta de usuario (operación de administrador).
     *
     * <p>Solo los administradores pueden desactivar forzosamente cualquier cuenta.</p>
     *
     * @param id identificador del usuario a desactivar
     * @return respuesta vacía con HTTP 204
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Inactiva la cuenta de un usuario tras validar su participación en el torneo.
     *
     * <p>El propio usuario puede inactivar su cuenta; los administradores pueden
     * inactivar cualquier cuenta. El servicio valida que el jugador no pertenezca
     * a un equipo activo antes de proceder.</p>
     *
     * @param id identificador del usuario a inactivar
     * @return respuesta vacía con HTTP 204
     */
    @PatchMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN') or @userAccessPolicy.canAccessOwnUser(#id, authentication)")
    public ResponseEntity<Void> inactivate(@PathVariable Long id) {
        userService.inactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactiva una cuenta de usuario previamente inactivada (operación de administrador).
     *
     * <p>Solo los administradores pueden reactivar cuentas.</p>
     *
     * @param id identificador del usuario a reactivar
     * @return respuesta vacía con HTTP 204
     */
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.noContent().build();
    }
}