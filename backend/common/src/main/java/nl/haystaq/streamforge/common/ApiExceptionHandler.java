package nl.haystaq.streamforge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Foutantwoorden bevatten wel een code en een correlatie-id, maar niet de
 * oorzaak. Die staat in de logs - en vaak in de logs van een andere service.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final String serviceName;

    public ApiExceptionHandler(@Value("${spring.application.name:unknown}") String serviceName) {
        this.serviceName = serviceName;
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleService(ServiceException exception) {
        log.warn("request rejected: code={} message={}", exception.code(), exception.getMessage());
        return ResponseEntity.status(exception.status()).body(body(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        log.error("unexpected failure in {}", serviceName, exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("internal_error", "something went wrong"));
    }

    private Map<String, Object> body(String code, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("service", serviceName);
        payload.put("code", code);
        payload.put("message", message);
        payload.put("requestId", MDC.get(CorrelationFilter.MDC_KEY));
        return payload;
    }
}
