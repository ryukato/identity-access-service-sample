package app.error;

public class FailToAddUserToApplicationException extends RuntimeException {
    public FailToAddUserToApplicationException(String message) {
        super(message);
    }

    public FailToAddUserToApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailToAddUserToApplicationException(Throwable cause) {
        super(cause);
    }

    public FailToAddUserToApplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
