package edu.dosw.users.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el almacén de datos.
 *
 * <p>Se mapea a HTTP 404 Not Found por {@link edu.dosw.users.exception.GlobalExceptionHandler}.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crea una nueva excepción de recurso no encontrado con el mensaje descriptivo indicado.
     *
     * @param message descripción del recurso que no pudo encontrarse
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}