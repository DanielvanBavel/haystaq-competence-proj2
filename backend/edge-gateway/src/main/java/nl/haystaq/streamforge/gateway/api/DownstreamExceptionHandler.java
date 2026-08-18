package nl.haystaq.streamforge.gateway.api;

import nl.haystaq.streamforge.common.DownstreamClient;
import nl.haystaq.streamforge.common.CorrelationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * De gateway geeft de status van de achterliggende service door, maar niet de
 * oorzaak: die staat in de logs van die service. Het correlatie-id in dit
 * antwoord is je ingang.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DownstreamExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DownstreamExceptionHandler.class);

    @ExceptionHandler(DownstreamClient.DownstreamException.class)
    public ResponseEntity<Map<String, Object>> handle(DownstreamClient.DownstreamException exception) {
        log.warn("upstream {} returned {}", exception.target(), exception.status());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "upstream_error");
        body.put("upstream", exception.target());
        body.put("upstreamStatus", exception.status());
        body.put("requestId", MDC.get(CorrelationFilter.MDC_KEY));
        return ResponseEntity.status(exception.status()).body(body);
    }
}
