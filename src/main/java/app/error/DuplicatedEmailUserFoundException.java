package app.error;

public class DuplicatedEmailUserFoundException extends RuntimeException {

    public DuplicatedEmailUserFoundException(String message) {
        super(message);
    }

    public DuplicatedEmailUserFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedEmailUserFoundException(Throwable cause) {
        super(cause);
    }

    public DuplicatedEmailUserFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
