package app.error;

public class SameApplicationNameExistException extends RuntimeException {
    public SameApplicationNameExistException(String message) {
        super(message);
    }
}
