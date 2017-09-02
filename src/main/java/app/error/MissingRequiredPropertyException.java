package app.error;

public class MissingRequiredPropertyException extends RuntimeException {
    public MissingRequiredPropertyException(String message) {
        super(message);
    }

    public MissingRequiredPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRequiredPropertyException(Throwable cause) {
        super(cause);
    }
}
