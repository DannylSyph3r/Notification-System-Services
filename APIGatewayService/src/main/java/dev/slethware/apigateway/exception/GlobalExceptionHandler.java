package dev.slethware.apigateway.exception;

import dev.slethware.apigateway.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Not Found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateRequest(DuplicateRequestException ex) {
        log.warn("Duplicate request detected: {}", ex.getMessage());
        // As per plan, return 200 OK with cached response
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(ex.getCachedResponse(), ex.getMessage()));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<?>> handleRestClientException(RestClientException ex) {
        log.error("Internal service communication failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Service Unavailable", "An internal service is currently unavailable. Please try again later."));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpClientError(HttpClientErrorException ex) {
        log.warn("HTTP client error from internal service: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        // Forward the error from the downstream service
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getStatusText(), ex.getResponseBodyAsString()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        log.warn("Validation failed for request: {}", ex.getMessage());
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiResponse<?> apiResponse = ApiResponse.error("Validation Failed", errors);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal Server Error", "An unexpected error occurred."));
    }
}