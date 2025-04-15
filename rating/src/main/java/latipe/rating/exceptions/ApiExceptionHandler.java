package latipe.rating.exceptions;

import static latipe.rating.constants.CONSTANTS.REQUEST_ID;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import latipe.rating.viewmodel.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the rating service.
 * Catches specific exceptions thrown by controllers or services and transforms them
 * into standardized API error responses (ExceptionResponse).
 * It also logs relevant information about the errors.
 */
@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String DATE_TIME_FORMAT = "HH:mm:ss yyyy-MM-dd";
    private static final String ERROR_LOG_FORMAT = "[Error] ID: {} URI: {}, Status: {}, Message: {}";

    /**
     * Handles NotFoundException.
     * Returns a 404 Not Found response.
     *
     * @param ex      The caught NotFoundException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex,
        WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(HttpStatus.NOT_FOUND.toString(),
            "Not Found",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            message, getServletPath(request));
        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.NOT_FOUND.value(), message);
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UnauthorizedException.
     * Returns a 401 Unauthorized response.
     *
     * @param ex      The caught UnauthorizedException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException ex,
        WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.UNAUTHORIZED.toString(),
            "Unauthorized",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), message,
            getServletPath(request));
        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.UNAUTHORIZED.value(), message);
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles BadRequestException.
     * Returns a 400 Bad Request response.
     *
     * @param ex      The caught BadRequestException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex,
        WebRequest request) {
        String message = ex.getMessage();
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), message,
            getServletPath(request));
        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.BAD_REQUEST.value(), message);
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentNotValidException (validation errors from @Valid).
     * Returns a 400 Bad Request response with detailed validation errors.
     *
     * @param ex The caught MethodArgumentNotValidException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response with validation details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ExceptionResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> String.format("Field '%s': %s", error.getField(), error.getDefaultMessage()))
            .toList();

        String message = "Request validation failed";
        ExceptionResponse exceptionResponse = new ExceptionResponse(HttpStatus.BAD_REQUEST.toString(), "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            message, getServletPath(request), errors);

        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.BAD_REQUEST.value(), String.format("%s: %s", message, errors));
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ConstraintViolationException (validation errors from method parameters, etc.).
     * Returns a 400 Bad Request response with detailed validation errors.
     *
     * @param ex The caught ConstraintViolationException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response with validation details.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(String.format("'%s': %s", violation.getPropertyPath(), violation.getMessage()));
        }
        String message = "Constraint violation";
        ExceptionResponse exceptionResponse = new ExceptionResponse(HttpStatus.BAD_REQUEST.toString(), "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            message, getServletPath(request), errors);

        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.BAD_REQUEST.value(), String.format("%s: %s", message, errors));
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MissingServletRequestParameterException.
     * Returns a 400 Bad Request response indicating a missing parameter.
     *
     * @param ex      The caught MissingServletRequestParameterException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response.
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, WebRequest request) {
        String paramName = ex.getParameterName();
        String message = "Required parameter '" + paramName + "' is missing";
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            message,
            getServletPath(request)
        );

        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.BAD_REQUEST.value(), message);
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles JsonParseException.
     * Returns a 400 Bad Request response indicating invalid JSON.
     *
     * @param ex      The caught JsonParseException.
     * @param request The current web request.
     * @return ResponseEntity containing the standardized error response.
     */
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ExceptionResponse> handleJsonParseException(
        JsonParseException ex, WebRequest request) {
        String message = "Invalid JSON format: " + ex.getOriginalMessage();

        ExceptionResponse exceptionResponse = new ExceptionResponse(
            HttpStatus.BAD_REQUEST.toString(),
            "Bad Request",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            message,
            getServletPath(request)
        );

        LOGGER.warn(ERROR_LOG_FORMAT, request.getAttribute(REQUEST_ID, 0),
            getServletPath(request),
            HttpStatus.BAD_REQUEST.value(), message);
        LOGGER.debug(ex.toString(), ex);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Helper method to extract the servlet path from the WebRequest.
     *
     * @param webRequest The current web request.
     * @return The servlet path as a String, or "N/A" if extraction fails.
     */
    private String getServletPath(WebRequest webRequest) {
        try {
            if (webRequest instanceof ServletWebRequest servletRequest) {
                return servletRequest.getRequest().getServletPath();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to extract servlet path from WebRequest", e);
        }
        return "N/A";
    }
}
