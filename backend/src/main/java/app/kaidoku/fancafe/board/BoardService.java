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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BoardService {

    /** NEW 뱃지 기준: 이 기간 안에 작성된 공개 글이 있으면 hasNew=true. */
    private static final Duration NEW_WINDOW = Duration.ofHours(48);

    /** 말머리 라벨 1개 최대 길이. */
    private static final int MAX_CATEGORY_LABEL_LENGTH = 20;

    /** 게시판당 말머리 라벨 최대 개수. */
    private static final int MAX_CATEGORY_COUNT = 12;

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    public BoardService(BoardRepository boardRepository, PostRepository postRepository) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
    }

    /** 노출된 게시판을 정렬 순서대로 반환(공개 사이트 카드 그리드용). hasNew는 단일 쿼리로 일괄 계산(N+1 회피). */
    public List<BoardResponse> listVisibleBoards() {
        List<Board> boards = boardRepository.findByVisibleTrueOrderBySortOrderAsc();
        Set<Long> recentIds = postRepository.boardIdsWithPostsAfter(LocalDateTime.now().minus(NEW_WINDOW));
        return boards.stream()
                .map(b -> BoardResponse.from(b, recentIds.contains(b.getId())))
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
                request.description(), request.sortOrder(), request.type(), request.writeRole(),
                joinCategories(request.categories()));
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void updateBoard(Long boardId, BoardUpdateRequest request) {
        Board board = getById(boardId);
        board.update(request.nameKr(), request.nameEn(), request.description(),
                request.sortOrder(), request.type(), request.writeRole(), request.visible(),
                joinCategories(request.categories()));
    }

    /** 말머리 라벨 목록 → 저장용 쉼표 문자열. 빈값 제거·트림·검증(길이/개수). 없으면 null. */
    private String joinCategories(List<String> categories) {
        if (categories == null) {
            return null;
        }
        List<String> cleaned = categories.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .toList();
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.size() > MAX_CATEGORY_COUNT) {
            throw ApiException.badRequest("말머리는 최대 " + MAX_CATEGORY_COUNT + "개까지 가능합니다.");
        }
        for (String label : cleaned) {
            if (label.length() > MAX_CATEGORY_LABEL_LENGTH) {
                throw ApiException.badRequest("말머리는 각 " + MAX_CATEGORY_LABEL_LENGTH + "자 이하여야 합니다: " + label);
            }
        }
        return String.join(",", cleaned);
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
