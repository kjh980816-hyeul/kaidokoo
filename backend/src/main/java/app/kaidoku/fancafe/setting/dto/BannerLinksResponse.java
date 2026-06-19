package app.kaidoku.fancafe.setting.dto;

/** 배너 외부 링크. 각 값은 URL 또는 null(미설정). 프론트 배너 버튼에서 사용. */
public record BannerLinksResponse(
        String youtube,
        String x,
        String seeme,
        String fancim,
        String fancimM
) {
}
