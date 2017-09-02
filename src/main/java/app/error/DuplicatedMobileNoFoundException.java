package app.error;

public class DuplicatedMobileNoFoundException extends RuntimeException {

    public DuplicatedMobileNoFoundException(String message) {
        super(message);
    }

    public DuplicatedMobileNoFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedMobileNoFoundException(Throwable cause) {
        super(cause);
    }

    public DuplicatedMobileNoFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
