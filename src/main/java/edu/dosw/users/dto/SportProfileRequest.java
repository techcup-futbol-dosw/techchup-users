package edu.dosw.users.dto;

import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload aceptado para las solicitudes de creación y actualización de perfil deportivo.
 *
 * <p>Los campos {@code userId} y {@code photoId} son resueltos por la capa de servicio
 * (desde la variable de ruta y el archivo subido, respectivamente) y por lo tanto
 * no forman parte del cuerpo de este request.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileRequest {

    private Position position;
    private Integer dorsalNumber;
    private boolean available;
}