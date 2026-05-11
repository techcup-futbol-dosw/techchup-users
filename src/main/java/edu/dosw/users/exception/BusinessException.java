package edu.dosw.users.exception;

/**
 * Thrown when an operation violates a business rule
 * (e.g. updating a sport profile while assigned to a team).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}