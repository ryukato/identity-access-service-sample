package app.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@ControllerAdvice
public class ExceptionHandleController {
    @ExceptionHandler({RecordNotFoundException.class, UsernameNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleRecordNotFoundException(HttpServletRequest request) throws URISyntaxException{
        URI requestUri = new URI(request.getRequestURI());
        return ResponseEntity.notFound().location(requestUri).build();
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleNullPointerException(HttpServletRequest request, NullPointerException npe) throws URISyntaxException{
        URI requestUri = new URI(request.getRequestURI());
        String errorMessage = Optional.ofNullable(npe.getMessage()).orElse("");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .location(requestUri)
                .body(new ErrorResponse(
                        errorMessage,
                        "NullPointerException",
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    @ExceptionHandler(DuplicatedEmailUserFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleDuplicatedEmail(HttpServletRequest request) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "DuplicatedEmailUserFoundException",
                "Same email address is already registered",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(DuplicatedMobileNoFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleDuplicatedMobileNo(HttpServletRequest request) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "DuplicatedEmailUserFoundException",
                "Same mobile phone no is already registered",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(SameUserNameFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleSameUserNameFound(HttpServletRequest request) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "SameUserNameFoundException",
                "Same user name is already registered",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(NoUserLoginCredentialException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleNoUserCredential(HttpServletRequest request) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "NoUserLoginCredentialException",
                "No user credential provided",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(InvalidGrantException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidGrant(HttpServletRequest request, InvalidGrantException ige) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "InvalidGrantException",
                ige.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ResponseEntity<?> handleAccessDeniedException(HttpServletRequest request) throws URISyntaxException{
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(FailToAddUserToApplicationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleFailToAddUserToApplication(HttpServletRequest request, FailToAddUserToApplicationException fae) throws URISyntaxException{
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "FailToAddUserToApplicationException",
                fae.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleInternalServerError(HttpServletRequest request, Throwable t) throws URISyntaxException{
        URI requestUri = new URI(request.getRequestURI());
        String errorMessage = Optional.ofNullable(t.getMessage()).orElse(t.getCause().getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .location(requestUri)
                .body(new ErrorResponse(
                        t.getClass().getSimpleName(),
                        errorMessage,
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    @ExceptionHandler(MissingRequiredPropertyException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingRequriedProperty(HttpServletRequest request, MissingRequiredPropertyException mrpe) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "MissingRequiredPropertyException",
                mrpe.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(SameApplicationNameExistException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleSameApplicationNameExist(HttpServletRequest request, SameApplicationNameExistException sane) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "SameApplicationNameExistException",
                sane.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    @ExceptionHandler(InvalidTenantAccountException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidTenantAccount(HttpServletRequest request, InvalidTenantAccountException itae) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "InvalidTenantAccountException",
                itae.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        ));
    }

    //
}
