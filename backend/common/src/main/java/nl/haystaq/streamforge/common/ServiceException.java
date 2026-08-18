package nl.haystaq.streamforge.common;

import org.springframework.http.HttpStatus;

/** Verwachte fout met een expliciete HTTP-status en een korte code. */
public class ServiceException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ServiceException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ServiceException notFound(String code, String message) {
        return new ServiceException(HttpStatus.NOT_FOUND, code, message);
    }

    public static ServiceException conflict(String code, String message) {
        return new ServiceException(HttpStatus.CONFLICT, code, message);
    }

    public static ServiceException forbidden(String code, String message) {
        return new ServiceException(HttpStatus.FORBIDDEN, code, message);
    }

    public static ServiceException badRequest(String code, String message) {
        return new ServiceException(HttpStatus.BAD_REQUEST, code, message);
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
