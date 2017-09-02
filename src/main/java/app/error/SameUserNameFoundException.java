package app.error;

public class SameUserNameFoundException extends RuntimeException {

    public SameUserNameFoundException(String message) {
        super(message);
    }

    public SameUserNameFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SameUserNameFoundException(Throwable cause) {
        super(cause);
    }

    public SameUserNameFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
