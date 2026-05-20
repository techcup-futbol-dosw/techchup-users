package edu.dosw.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Manejador global de excepciones HTTP para el microservicio de usuarios.
 *
 * <p>Centraliza el mapeo de excepciones de dominio a respuestas HTTP estructuradas:</p>
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 *   <li>{@link BusinessException}         → 409 Conflict</li>
 * </ul>
 *
 * @author CodeForge
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recurso no encontrado retornando HTTP 404.
     *
     * @param ex excepción lanzada cuando el recurso solicitado no existe
     * @return respuesta 404 con el mensaje de error en formato JSON
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maneja excepciones de regla de negocio retornando HTTP 409.
     *
     * @param ex excepción lanzada cuando se viola una regla de negocio
     * @return respuesta 409 con el mensaje de error en formato JSON
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}