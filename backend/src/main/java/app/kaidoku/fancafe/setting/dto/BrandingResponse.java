package app.kaidoku.fancafe.setting.dto;

/** 사이트 브랜딩(공개). logoUrl이 null이면 프론트는 기본 엠블럼을 쓴다. */
public record BrandingResponse(String logoUrl) {
}
