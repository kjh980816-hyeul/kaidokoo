package app.kaidoku.fancafe.board;

import app.kaidoku.fancafe.board.dto.BoardResponse;
import app.kaidoku.fancafe.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    /** 노출된 게시판을 정렬 순서대로 반환(공개 사이트 카드 그리드용). */
    public List<BoardResponse> listVisibleBoards() {
        return boardRepository.findByVisibleTrueOrderBySortOrderAsc().stream()
                .map(BoardResponse::from)
                .toList();
    }

    /** code로 게시판 조회. 없거나 숨김이면 404. */
    public Board getVisibleByCode(String code) {
        Board board = boardRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("게시판을 찾을 수 없습니다: " + code));
        if (!board.isVisible()) {
            throw ApiException.notFound("게시판을 찾을 수 없습니다: " + code);
        }
        return board;
    }
}
