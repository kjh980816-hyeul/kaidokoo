package app.kaidoku.fancafe.common;

import org.springframework.http.HttpStatus;

/** 도메인 예외. 전역 핸들러에서 표준 에러 응답으로 매핑된다(스택트레이스 노출 금지). */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
