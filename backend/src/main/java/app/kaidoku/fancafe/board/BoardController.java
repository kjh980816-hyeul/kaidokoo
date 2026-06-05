package app.kaidoku.fancafe.board;

import app.kaidoku.fancafe.board.dto.BoardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /** 공개 게시판 목록. */
    @GetMapping
    public List<BoardResponse> list() {
        return boardService.listVisibleBoards();
    }
}
