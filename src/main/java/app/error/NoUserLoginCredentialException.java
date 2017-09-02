package app.error;

public class NoUserLoginCredentialException extends RuntimeException {

    //default
    public NoUserLoginCredentialException() {
    }

    public NoUserLoginCredentialException(String message) {
        super(message);
    }

    public NoUserLoginCredentialException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUserLoginCredentialException(Throwable cause) {
        super(cause);
    }

    public NoUserLoginCredentialException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
