package app.kaidoku.fancafe.board;

/**
 * 게시판 표시 형식. 글 데이터는 모두 동일하고 목록 렌더링만 형식별로 달라진다(고추밭 방식).
 * <ul>
 *   <li>GENERAL — 목록형(제목 한 줄)</li>
 *   <li>GALLERY — 갤러리(이미지 그리드)</li>
 *   <li>CARD — 카드형(썸네일+요약)</li>
 *   <li>VIDEO — 영상형(16:9 썸네일)</li>
 *   <li>LETTER — 편지형(편지지 카드)</li>
 *   <li>RANK — 랭킹(좋아요순 정렬)</li>
 * </ul>
 */
public enum BoardType {
    GENERAL,
    GALLERY,
    CARD,
    VIDEO,
    LETTER,
    RANK
}
