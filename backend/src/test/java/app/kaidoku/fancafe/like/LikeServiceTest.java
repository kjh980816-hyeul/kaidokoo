package app.kaidoku.fancafe.like;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.like.dto.LikeResponse;
import app.kaidoku.fancafe.member.Member;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock PostLikeRepository postLikeRepository;
    @Mock PostRepository postRepository;

    @InjectMocks LikeService likeService;

    private Member member(long id) {
        Member m = Member.create(Provider.GOOGLE, "u" + id, "선원");
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    private Post post(long id, int likeCount) {
        Board board = Board.create("free", "정박지", null, null, 1, BoardType.GENERAL, Role.MEMBER);
        Post p = Post.create(board, member(99L), "제목", "본문");
        ReflectionTestUtils.setField(p, "id", id);
        ReflectionTestUtils.setField(p, "likeCount", likeCount);
        return p;
    }

    @Test
    void toggle_addsLikeAndIncrementsCount() {
        Member m = member(5L);
        Post p = post(1L, 0);
        when(postRepository.findById(1L)).thenReturn(Optional.of(p));
        when(postLikeRepository.findByMember_IdAndPost_Id(5L, 1L)).thenReturn(Optional.empty());

        LikeResponse result = likeService.toggle(1L, m);

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(1);
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void toggle_removesLikeAndDecrementsCount() {
        Member m = member(5L);
        Post p = post(1L, 1);
        PostLike existing = PostLike.create(m, p);
        when(postRepository.findById(1L)).thenReturn(Optional.of(p));
        when(postLikeRepository.findByMember_IdAndPost_Id(5L, 1L)).thenReturn(Optional.of(existing));

        LikeResponse result = likeService.toggle(1L, m);

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isZero();
        verify(postLikeRepository).delete(existing);
    }
}
