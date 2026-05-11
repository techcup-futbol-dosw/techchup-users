package edu.dosw.users.exception;

/**
 * Thrown when a requested resource does not exist in the data store.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}