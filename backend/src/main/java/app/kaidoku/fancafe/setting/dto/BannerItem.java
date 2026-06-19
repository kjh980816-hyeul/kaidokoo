package app.kaidoku.fancafe.setting.dto;

/**
 * 배너 항목 한 개(이름 + 링크 + 아이콘). 관리자가 자유롭게 추가/삭제한다.
 * iconUrl은 선택(없으면 프론트가 기본 엠블럼 사용). 검증은 서비스에서.
 */
public record BannerItem(String label, String url, String iconUrl) {
}
