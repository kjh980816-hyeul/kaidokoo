package app.kaidoku.fancafe.setting.dto;

/** 사이트 로고 설정 요청(관리자). 업로드 경로(/uploads/...) 또는 외부 URL, 빈값이면 해제. */
public record BrandingRequest(String logoUrl) {
}
