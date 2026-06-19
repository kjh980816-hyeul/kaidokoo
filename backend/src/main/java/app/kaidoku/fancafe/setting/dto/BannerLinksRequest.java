package app.kaidoku.fancafe.setting.dto;

/** 배너 외부 링크 설정 요청(관리자). 각 값은 URL(http/https) 또는 빈값(해제). 검증은 서비스에서. */
public record BannerLinksRequest(
        String youtube,
        String x,
        String seeme,
        String fancim,
        String fancimM
) {
}
