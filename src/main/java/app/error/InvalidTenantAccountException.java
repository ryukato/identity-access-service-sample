package app.error;

public class InvalidTenantAccountException extends RuntimeException {
    public InvalidTenantAccountException(String message) {
        super(message);
    }
}
