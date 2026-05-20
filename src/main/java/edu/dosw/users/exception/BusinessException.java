package edu.dosw.users.exception;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio
 * (p. ej. actualizar un perfil deportivo mientras se está asignado a un equipo).
 *
 * <p>Se mapea a HTTP 409 Conflict por {@link edu.dosw.users.exception.GlobalExceptionHandler}.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public class BusinessException extends RuntimeException {

    /**
     * Crea una nueva excepción de negocio con el mensaje descriptivo indicado.
     *
     * @param message descripción de la regla de negocio violada
     */
    public BusinessException(String message) {
        super(message);
    }
}