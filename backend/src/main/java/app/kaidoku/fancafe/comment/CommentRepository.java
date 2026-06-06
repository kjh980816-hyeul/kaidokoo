package app.kaidoku.fancafe.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 글의 댓글 목록. 작성자·등급 fetch join(N+1 회피), 오래된 순.
     * HIDDEN(신고/관리자 숨김)만 제외하고 DELETED는 포함한다 → 대댓글 스레드를 보존하고
     * 본문은 "삭제된 댓글" 자리표시로 렌더한다.
     */
    @Query("""
            select c from Comment c
            join fetch c.author a
            left join fetch a.grade
            where c.post.id = :postId and c.status <> app.kaidoku.fancafe.comment.CommentStatus.HIDDEN
            order by c.createdAt asc
            """)
    List<Comment> findVisibleForPost(@Param("postId") Long postId);
}
