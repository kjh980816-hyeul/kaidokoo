package app.kaidoku.fancafe.comment;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberStatus;
import app.kaidoku.fancafe.member.Provider;
import app.kaidoku.fancafe.post.Post;
import app.kaidoku.fancafe.post.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;

    @InjectMocks CommentService commentService;

    private Member member(long id, Role role) {
        Member m = Member.create(Provider.GOOGLE, "u" + id, "선원");
        ReflectionTestUtils.setField(m, "id", id);
        ReflectionTestUtils.setField(m, "role", role);
        ReflectionTestUtils.setField(m, "status", MemberStatus.ACTIVE);
        return m;
    }

    private Post post(long id) {
        Board board = Board.create("free", "정박지", null, null, 1, BoardType.GENERAL, Role.MEMBER);
        Post p = Post.create(board, member(99L, Role.MEMBER), "제목", "본문");
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private Comment comment(long id, Member author) {
        Comment c = Comment.create(post(1L), author, null, "안녕");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    @Test
    void delete_softDeletesForAuthor() {
        Member author = member(5L, Role.MEMBER);
        Comment c = comment(7L, author);
        when(commentRepository.findById(7L)).thenReturn(Optional.of(c));

        commentService.delete(7L, author);

        assertThat(c.isDeleted()).isTrue();
    }

    @Test
    void delete_allowedForAdmin() {
        Comment c = comment(7L, member(5L, Role.MEMBER));
        Member admin = member(9L, Role.ADMIN);
        when(commentRepository.findById(7L)).thenReturn(Optional.of(c));

        commentService.delete(7L, admin);

        assertThat(c.isDeleted()).isTrue();
    }

    @Test
    void delete_forbiddenForOtherMember() {
        Comment c = comment(7L, member(5L, Role.MEMBER));
        Member other = member(9L, Role.MEMBER);
        when(commentRepository.findById(7L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> commentService.delete(7L, other))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("삭제");
        assertThat(c.isDeleted()).isFalse();
    }

    @Test
    void create_throwsWhenPostMissing() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(1L, member(5L, Role.MEMBER),
                new app.kaidoku.fancafe.comment.dto.CommentCreateRequest("내용", null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("글을 찾을 수 없습니다");
    }
}
