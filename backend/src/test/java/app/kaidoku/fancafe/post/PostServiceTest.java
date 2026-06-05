package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardService;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.Provider;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock BoardService boardService;
    @Mock MemberRepository memberRepository;

    @InjectMocks PostService postService;

    private Board sampleBoard() {
        Board board = Board.create("free", "정박지", "Harbor", null, 1,
                app.kaidoku.fancafe.board.BoardType.GENERAL, app.kaidoku.fancafe.common.Role.MEMBER);
        ReflectionTestUtils.setField(board, "id", 10L);
        return board;
    }

    @Test
    void getDetail_increasesViewCount() {
        Board board = sampleBoard();
        Member author = Member.create(Provider.GOOGLE, "u1", "선원");
        Post post = Post.create(board, author, "제목", "본문");
        ReflectionTestUtils.setField(post, "id", 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostDetailResponse result = postService.getDetail(1L);

        assertThat(result.viewCount()).isEqualTo(1);
        assertThat(result.title()).isEqualTo("제목");
        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Test
    void getDetail_throwsWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getDetail(99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    void getDetail_throwsWhenDeleted() {
        Post deleted = Post.create(sampleBoard(), Member.create(Provider.GOOGLE, "u1", "선원"), "t", "c");
        ReflectionTestUtils.setField(deleted, "status", PostStatus.DELETED);
        when(postRepository.findById(2L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> postService.getDetail(2L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void create_savesAndReturnsId() {
        Board board = sampleBoard();
        Member author = Member.create(Provider.GOOGLE, "u1", "선원");
        ReflectionTestUtils.setField(author, "id", 5L);
        when(boardService.getVisibleByCode("free")).thenReturn(board);
        when(memberRepository.findById(5L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 42L);
            return p;
        });

        Long id = postService.create(new PostCreateRequest("free", 5L, "제목", "본문"));

        assertThat(id).isEqualTo(42L);
    }

    @Test
    void create_throwsWhenAuthorMissing() {
        when(boardService.getVisibleByCode("free")).thenReturn(sampleBoard());
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(new PostCreateRequest("free", 999L, "t", "c")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("작성자");
    }
}
