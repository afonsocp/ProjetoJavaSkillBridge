package br.com.skillbridge.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + messageSource.getMessage(error, locale);
                    }
                    return messageSource.getMessage(error, locale);
                })
                .toList();

        String message = messageSource.getMessage("error.validation", null, "Validation error", locale);
        HttpStatus httpStatus = status instanceof HttpStatus ? (HttpStatus) status : HttpStatus.BAD_REQUEST;
        ApiError apiError = ApiError.fromStatus(httpStatus, message, extractPath(request), details);
        return ResponseEntity.status(httpStatus).body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : messageSource.getMessage("error.notfound", null, "Resource not found", locale);
        ApiError apiError = ApiError.fromStatus(HttpStatus.NOT_FOUND, message, request.getRequestURI(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.fromStatus(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.unauthorized", null, "Unauthorized", locale);
        ApiError apiError = ApiError.fromStatus(HttpStatus.UNAUTHORIZED, message, request.getRequestURI(), List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.forbidden", null, "Forbidden", locale);
        ApiError apiError = ApiError.fromStatus(HttpStatus.FORBIDDEN, message, request.getRequestURI(), List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.internal", null, "Internal error", locale);
        ApiError apiError = ApiError.fromStatus(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI(), List.of(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "";
    }
}

