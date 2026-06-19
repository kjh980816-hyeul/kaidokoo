package app.kaidoku.fancafe.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId);

    long countByCommentId(Long commentId);

    /** 댓글 묶음의 좋아요 수 일괄 집계(목록 N+1 회피). */
    @Query("""
            select cl.commentId as commentId, count(cl) as count
            from CommentLike cl
            where cl.commentId in :ids
            group by cl.commentId
            """)
    List<CommentLikeCount> countByCommentIds(@Param("ids") List<Long> ids);

    /** 회원이 좋아요한 댓글 id 목록(목록에서 내 좋아요 여부 일괄 판정). */
    @Query("select cl.commentId from CommentLike cl where cl.memberId = :memberId and cl.commentId in :ids")
    List<Long> findLikedCommentIds(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);

    /** 집계 프로젝션. */
    interface CommentLikeCount {
        Long getCommentId();
        long getCount();
    }
}
