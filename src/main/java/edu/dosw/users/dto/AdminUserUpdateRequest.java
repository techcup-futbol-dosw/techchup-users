package edu.dosw.users.dto;

import edu.dosw.users.enums.SchoolRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO aceptado por los administradores para actualizar la información básica de un usuario.
 *
 * <p>Contiene únicamente los campos que un administrador tiene permitido modificar.
 * Los demás campos (correo, contraseña, identificación, género, etc.) son ignorados
 * por el mapper y no se incluyen en este request.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest implements Serializable {

    private String fullName;

    private SchoolRelation schoolRelation;

    private String academicProgram;

    private Integer semester;
}
