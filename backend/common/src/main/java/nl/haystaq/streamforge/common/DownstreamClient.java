package nl.haystaq.streamforge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Kleine wrapper om RestClient die het correlatie-id doorgeeft en elke aanroep
 * naar een andere service logt, inclusief duur en status.
 */
public class DownstreamClient {

    private static final Logger log = LoggerFactory.getLogger(DownstreamClient.class);

    private final String targetName;
    private final RestClient restClient;

    public DownstreamClient(String targetName, String baseUrl, Duration connectTimeout, Duration readTimeout) {
        this.targetName = targetName;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) connectTimeout.toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public <T> T get(String uri, Class<T> type) {
        long started = System.nanoTime();
        try {
            T result = restClient.get()
                    .uri(uri)
                    .header(CorrelationFilter.HEADER, MDC.get(CorrelationFilter.MDC_KEY))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DownstreamException(targetName, response.getStatusCode().value(),
                                "call to " + targetName + " failed with " + response.getStatusCode());
                    })
                    .body(type);
            log.info("downstream {} GET {} ok in {}ms", targetName, uri, millisSince(started));
            return result;
        } catch (DownstreamException exception) {
            log.warn("downstream {} GET {} failed status={} in {}ms",
                    targetName, uri, exception.status(), millisSince(started));
            throw exception;
        } catch (RuntimeException exception) {
            int status = isTimeout(exception) ? 504 : 503;
            log.error("downstream {} GET {} failed after {}ms (status {})",
                    targetName, uri, millisSince(started), status, exception);
            throw new DownstreamException(targetName, status, exception.getMessage());
        }
    }

    public <T> T post(String uri, Object payload, Class<T> type) {
        long started = System.nanoTime();
        try {
            T result = restClient.post()
                    .uri(uri)
                    .header(CorrelationFilter.HEADER, MDC.get(CorrelationFilter.MDC_KEY))
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DownstreamException(targetName, response.getStatusCode().value(),
                                "call to " + targetName + " failed with " + response.getStatusCode());
                    })
                    .body(type);
            log.info("downstream {} POST {} ok in {}ms", targetName, uri, millisSince(started));
            return result;
        } catch (DownstreamException exception) {
            log.warn("downstream {} POST {} failed status={} in {}ms",
                    targetName, uri, exception.status(), millisSince(started));
            throw exception;
        } catch (RuntimeException exception) {
            int status = isTimeout(exception) ? 504 : 503;
            log.error("downstream {} POST {} failed after {}ms (status {})",
                    targetName, uri, millisSince(started), status, exception);
            throw new DownstreamException(targetName, status, exception.getMessage());
        }
    }

    private static long millisSince(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000;
    }

    /** Een read timeout is iets anders dan een service die niet bestaat. */
    private static boolean isTimeout(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof java.net.SocketTimeoutException) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return false;
    }

    public static class DownstreamException extends RuntimeException {

        private final String target;
        private final int status;

        public DownstreamException(String target, int status, String message) {
            super(message);
            this.target = target;
            this.status = status;
        }

        public String target() {
            return target;
        }

        public int status() {
            return status;
        }
    }
}
