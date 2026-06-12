package app.kaidoku.fancafe.like;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.like.dto.LikeResponse;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.Post;
import app.kaidoku.fancafe.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 글 좋아요 토글/조회. (회원,글) 유니크 제약과 post.like_count를 일관되게 유지한다. */
@Service
@Transactional(readOnly = true)
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    public LikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }

    /** 좋아요 현황. 비로그인(member=null)이면 liked=false. */
    public LikeResponse getStatus(Long postId, Member member) {
        Post post = getActivePost(postId);
        boolean liked = member != null
                && postLikeRepository.existsByMember_IdAndPost_Id(member.getId(), post.getId());
        return new LikeResponse(liked, post.getLikeCount());
    }

    /** 좋아요 토글. 이미 눌렀으면 취소(카운트 -1), 아니면 추가(+1). 카운트는 DB에서 원자적으로 증감. */
    @Transactional
    public LikeResponse toggle(Long postId, Member member) {
        Post post = getActivePost(postId);
        return postLikeRepository.findByMember_IdAndPost_Id(member.getId(), post.getId())
                .map(existing -> {
                    postLikeRepository.delete(existing);
                    postRepository.decrementLikeCount(post.getId());
                    return new LikeResponse(false, Math.max(0, post.getLikeCount() - 1));
                })
                .orElseGet(() -> {
                    postLikeRepository.save(PostLike.create(member, post));
                    postRepository.incrementLikeCount(post.getId());
                    return new LikeResponse(true, post.getLikeCount() + 1);
                });
    }

    private Post getActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        return post;
    }
}
