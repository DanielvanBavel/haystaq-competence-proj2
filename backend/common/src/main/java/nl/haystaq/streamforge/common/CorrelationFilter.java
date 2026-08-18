package nl.haystaq.streamforge.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Zorgt dat elke request een correlatie-id heeft en dat dat id in elke logregel
 * van elke service terugkomt. Dit is de draad waarlangs je een storing door de
 * keten kunt volgen.
 */
@Component
public class CorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger("access");

    private final String serviceName;

    public CorrelationFilter(@Value("${spring.application.name:unknown}") String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        MDC.put("service", serviceName);
        response.setHeader(HEADER, requestId);

        long started = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - started) / 1_000_000;
            MDC.put("durationMs", String.valueOf(millis));
            MDC.put("status", String.valueOf(response.getStatus()));
            log.info("{} {} -> {} in {}ms", request.getMethod(), path(request), response.getStatus(), millis);
            MDC.clear();
        }
    }

    private static String path(HttpServletRequest request) {
        return request.getQueryString() == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + request.getQueryString();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }
}
