package app.kaidoku.fancafe.setting.dto;

import java.util.List;

/** 배너 항목 일괄 설정 요청(관리자). items 전체로 교체(upsert). 검증은 서비스에서. */
public record BannerItemsRequest(List<BannerItem> items) {
}
