package app.kaidoku.fancafe.board;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.board.dto.BoardAdminResponse;
import app.kaidoku.fancafe.board.dto.BoardCreateRequest;
import app.kaidoku.fancafe.board.dto.BoardUpdateRequest;
import app.kaidoku.fancafe.member.Member;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 게시판 관리(관리자). 모든 진입점에서 ADMIN 서버 재검증. */
@RestController
@RequestMapping("/api/admin/boards")
public class BoardAdminController {

    private final BoardService boardService;

    public BoardAdminController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardAdminResponse> list(@CurrentMember Member admin) {
        AdminGuard.require(admin);
        return boardService.listAllForAdmin();
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> create(@CurrentMember Member admin,
                                                     @Valid @RequestBody BoardCreateRequest request) {
        AdminGuard.require(admin);
        return ResponseEntity.ok(Map.of("id", boardService.createBoard(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@CurrentMember Member admin,
                                       @PathVariable Long id,
                                       @Valid @RequestBody BoardUpdateRequest request) {
        AdminGuard.require(admin);
        boardService.updateBoard(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentMember Member admin, @PathVariable Long id) {
        AdminGuard.require(admin);
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
