package app.error;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private final String message;
    private final String exception;
    private final String path;
    private final HttpStatus status;
    private final Long timestamp;

    public ErrorResponse(String type, String message, String path, HttpStatus httpStatus) {
        this.message = message;
        this.exception = type;
        this.path = path;
        this.status = httpStatus;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    public String getPath() {
        return path;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
