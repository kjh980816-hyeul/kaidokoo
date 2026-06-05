package app.kaidoku.fancafe.common;

/** 클라이언트로 나가는 표준 에러 형태(내부 메시지/스택 비노출). */
public record ErrorResponse(int status, String error, String message) {
}
