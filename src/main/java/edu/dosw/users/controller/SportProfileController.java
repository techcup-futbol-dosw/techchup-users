package edu.dosw.users.controller;

import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.service.ImageService;
import edu.dosw.users.service.ISportProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para la gestión de perfiles deportivos de jugadores.
 *
 * <p>Ruta base: {@code /api/sport-profiles}</p>
 *
 * <p>Resumen de control de acceso:
 * <ul>
 *   <li>Cualquier usuario autenticado — puede leer cualquier perfil deportivo o foto.</li>
 *   <li>Propietario del perfil (JUGADOR) — puede crear, actualizar y alternar su propio perfil.</li>
 *   <li>ADMINISTRADOR — acceso completo.</li>
 * </ul>
 * </p>
 *
 * @author CodeForge
 * @since 1.0
 */
@RestController
@RequestMapping("/api/sport-profiles")
@RequiredArgsConstructor
public class SportProfileController {

    private final ISportProfileService sportProfileService;
    private final SportProfileMapper sportProfileMapper;
    private final ImageService imageService;

    /**
     * Retorna el perfil deportivo con el identificador indicado.
     *
     * <p>Legible por cualquier usuario autenticado (capitanes buscando jugadores, etc.).</p>
     *
     * @param id identificador del perfil deportivo
     * @return respuesta con los datos del perfil deportivo
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(sportProfileService.getById(id)));
    }

    /**
     * Retorna el perfil deportivo asociado al usuario indicado.
     *
     * <p>Accesible por el propietario del perfil, capitanes (búsqueda de jugadores)
     * y administradores.</p>
     *
     * @param userId identificador del usuario propietario del perfil
     * @return respuesta con los datos del perfil deportivo
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@sportProfileAccessPolicy.canAccessOwnSportProfile(#userId, authentication) or hasRole('CAPITAN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(sportProfileService.getByUserId(userId)));
    }

    /**
     * Crea un nuevo perfil deportivo para el usuario especificado.
     *
     * <p>Solo el propietario puede crear su propio perfil deportivo.</p>
     *
     * @param userId  identificador del usuario propietario (parámetro de ruta)
     * @param request datos del perfil como parte JSON del multipart
     * @param photo   foto del jugador (opcional)
     * @return respuesta con los datos del perfil creado (HTTP 201)
     */
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@sportProfileAccessPolicy.canAccessOwnSportProfile(#userId, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> create(
            @PathVariable Long userId,
            @RequestPart("profile") SportProfileRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sportProfileMapper.toResponse(
                        sportProfileService.create(userId, sportProfileMapper.toModel(request), photo)));
    }

    /**
     * Actualiza un perfil deportivo existente.
     *
     * <p>Solo el propietario del perfil puede actualizarlo; la capa de servicio
     * impone la regla de "no pertenecer a un equipo activo".</p>
     *
     * @param id      identificador del perfil deportivo
     * @param request nuevos datos del perfil como parte JSON del multipart
     * @param photo   nueva foto del jugador (opcional); si se omite se conserva la existente
     * @return respuesta con los datos del perfil actualizado
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@sportProfileAccessPolicy.canModifyOwnSportProfile(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> update(
            @PathVariable Long id,
            @RequestPart("profile") SportProfileRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(
                        sportProfileService.update(id, sportProfileMapper.toModel(request), photo)));
    }

    /**
     * Actualiza el indicador de disponibilidad del perfil deportivo.
     *
     * <p>Solo el propietario del perfil puede modificar su propia disponibilidad.</p>
     *
     * @param id        identificador del perfil deportivo
     * @param available nuevo valor de disponibilidad (parámetro de consulta)
     * @return respuesta vacía con HTTP 204
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("@sportProfileAccessPolicy.canModifyOwnSportProfile(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        sportProfileService.updateAvailability(id, available);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna el contenido binario de la foto de perfil de un jugador.
     *
     * <p>Legible por cualquier usuario autenticado.</p>
     *
     * @param photoId identificador del documento MongoDB que contiene la foto
     * @return bytes de la imagen con el {@code Content-Type} correcto, o 404 si no existe
     * @throws ResourceNotFoundException si no existe ninguna foto con el identificador indicado
     */
    @GetMapping("/photos/{photoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String photoId) {
        PlayerPhoto photo = imageService.getPhoto(photoId);
        if (photo == null) {
            throw new ResourceNotFoundException("Photo not found with id: " + photoId);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(photo.getData());
    }
}