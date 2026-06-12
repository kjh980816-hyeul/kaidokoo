package app.kaidoku.fancafe.board;

import app.kaidoku.fancafe.board.dto.BoardAdminResponse;
import app.kaidoku.fancafe.board.dto.BoardCreateRequest;
import app.kaidoku.fancafe.board.dto.BoardResponse;
import app.kaidoku.fancafe.board.dto.BoardUpdateRequest;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.post.PostRepository;
import app.kaidoku.fancafe.post.PostStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    public BoardService(BoardRepository boardRepository, PostRepository postRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
    }

    /** 노출된 게시판을 정렬 순서대로 반환(공개 사이트 카드 그리드용). */
    public List<BoardResponse> listVisibleBoards() {
        return boardRepository.findByVisibleTrueOrderBySortOrderAsc().stream()
                .map(BoardResponse::from)
                .toList();
    }

    /** 관리자용: 숨김 포함 전체 게시판. */
    public List<BoardAdminResponse> listAllForAdmin() {
        return boardRepository.findAllByOrderBySortOrderAsc().stream()
                .map(BoardAdminResponse::from)
                .toList();
    }

    @Transactional
    public Long createBoard(BoardCreateRequest request) {
        if (boardRepository.existsByCode(request.code())) {
            throw ApiException.conflict("이미 존재하는 게시판 코드입니다: " + request.code());
        }
        Board board = Board.create(request.code(), request.nameKr(), request.nameEn(),
                request.description(), request.sortOrder(), request.type(), request.writeRole());
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void updateBoard(Long boardId, BoardUpdateRequest request) {
        Board board = getById(boardId);
        board.update(request.nameKr(), request.nameEn(), request.description(),
                request.sortOrder(), request.type(), request.writeRole(), request.visible());
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = getById(boardId);
        // 소프트삭제(DELETED)된 글만 남은 게시판은 삭제 가능해야 한다.
        if (postRepository.existsByBoard_IdAndStatusNot(boardId, PostStatus.DELETED)) {
            throw ApiException.conflict("글이 있는 게시판은 삭제할 수 없습니다. 먼저 글을 정리하거나 숨김 처리하세요.");
        }
        boardRepository.delete(board);
    }

    private Board getById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> ApiException.notFound("게시판을 찾을 수 없습니다: " + boardId));
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
