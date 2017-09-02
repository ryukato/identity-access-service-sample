package app.error;

public class InvalidApplicationOwnerException extends RuntimeException {
    public InvalidApplicationOwnerException(String message) {
        super(message);
    }

    public InvalidApplicationOwnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
