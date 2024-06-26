package latipe.cart.exceptions;

import static latipe.cart.constants.CONSTANTS.REQUEST_ID;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import latipe.cart.viewmodel.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    private static final String ERROR_LOG_FORMAT = "[Error] ID: {} URI: {}, ErrorCode: {}, Message: {}";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex,
        WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse ExceptionResponse = new ExceptionResponse(HttpStatus.NOT_FOUND.toString(),
            "NotFound",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            message, request.getContextPath());
        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            404, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(ExceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException ex,
        WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse ExceptionResponse = new ExceptionResponse(
            HttpStatus.UNAUTHORIZED.toString(),
            "Unauthorized ",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")), message,
            request.getContextPath());
        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            401, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(ExceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex,
        WebRequest request) {
        String message = ex.getMessage();
        var ExceptionResponse = new ExceptionResponse(HttpStatus.BAD_REQUEST.toString(),
            "Bad request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")), message,
            request.getContextPath());
        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            400, message);
        return ResponseEntity.badRequest().body(ExceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .toList();

        ExceptionResponse ExceptionResponse = new ExceptionResponse("400", "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            "Request information is not valid", "", errors);
        return ResponseEntity.badRequest().body(ExceptionResponse);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                violation.getPropertyPath() + ": " + violation.getMessage());
        }
        var ExceptionResponse = new ExceptionResponse("400", "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            "Request information is not valid", "", errors);
        return ResponseEntity.badRequest().body(ExceptionResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, WebRequest request) {
        String paramName = ex.getParameterName();
        String message = "Required parameter '" + paramName + "' is missing";

        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            message,
            request.getContextPath()
        );

        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            400, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameter(
        ServletRequestBindingException ex, WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.UNAUTHORIZED.toString(),
            "Unauthorized",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            "Missing authentication header",
            request.getContextPath()
        );

        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            400, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ExceptionResponse> handleJsonParseException(
        JsonParseException ex, WebRequest request) {
        String message = "Invalid JSON format";

        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            message,
            request.getContextPath()
        );

        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            400, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SignInRequiredException.class})
    public ResponseEntity<Object> handleSignInRequired(SignInRequiredException ex,
        WebRequest request) {
        String message = ex.getMessage();
        var ExceptionResponse = new ExceptionResponse("403", "Authentication required",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")), message,
            "");

        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            403, message);
        log.debug(ex.toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionResponse);
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<Object> handleForbidden(NotFoundException ex, WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse ExceptionResponse = new ExceptionResponse(HttpStatus.FORBIDDEN.toString(),
            "Forbidden",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd")),
            message, request.getContextPath());

        log.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            this.getServletPath(request),
            403, message);
        log.debug(ex.toString());
        return new ResponseEntity<>(ExceptionResponse, HttpStatus.FORBIDDEN);
    }

    private String getServletPath(WebRequest webRequest) {
        ServletWebRequest servletRequest = (ServletWebRequest) webRequest;
        return servletRequest.getRequest().getServletPath();
    }
}
