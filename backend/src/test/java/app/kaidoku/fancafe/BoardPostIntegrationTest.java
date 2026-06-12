package app.kaidoku.fancafe;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardRepository;
import app.kaidoku.fancafe.board.dto.BoardResponse;
import app.kaidoku.fancafe.board.BoardService;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.Provider;
import app.kaidoku.fancafe.post.PostService;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import app.kaidoku.fancafe.post.dto.PostSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 수직 슬라이스 통합 검증: Flyway 스키마(H2/MySQL 모드) → JPA validate → repository/service 왕복.
 * 스키마-엔티티 매핑이 어긋나면 컨텍스트 로딩 자체가 실패하므로, 통과 = 슬라이스 배선 정상.
 */
@SpringBootTest
@Transactional
class BoardPostIntegrationTest {

    @Autowired BoardRepository boardRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired BoardService boardService;
    @Autowired PostService postService;

    private Board newBoard(String code, int sort) {
        return Board.create(code, "테스트게시판", null, null, sort,
                app.kaidoku.fancafe.board.BoardType.GENERAL, app.kaidoku.fancafe.common.Role.MEMBER);
    }

    @Test
    void createThenListAndRead_roundTrip() {
        Board board = boardRepository.save(newBoard("itest", 1));
        Member author = memberRepository.save(Member.create(Provider.GOOGLE, "itest-1", "통합선원"));

        Long postId = postService.create(
                new PostCreateRequest(board.getCode(), "통합 제목", "통합 본문"), author);

        List<PostSummaryResponse> list = postService.listByBoard(board.getCode());
        assertThat(list).extracting(PostSummaryResponse::title).contains("통합 제목");
        assertThat(list).extracting(PostSummaryResponse::authorNickname).contains("통합선원");

        PostDetailResponse detail = postService.getDetail(postId);
        assertThat(detail.content()).isEqualTo("통합 본문");
        assertThat(detail.boardCode()).isEqualTo("itest");
        assertThat(detail.viewCount()).isEqualTo(1);
    }

    @Test
    void listVisibleBoards_excludesHidden() {
        boardRepository.save(newBoard("visible-b", 1));
        Board hidden = newBoard("hidden-b", 2);
        hidden.changeVisibility(false);
        boardRepository.save(hidden);

        List<BoardResponse> boards = boardService.listVisibleBoards();

        assertThat(boards).extracting(BoardResponse::code).contains("visible-b");
        assertThat(boards).extracting(BoardResponse::code).doesNotContain("hidden-b");
    }
}
