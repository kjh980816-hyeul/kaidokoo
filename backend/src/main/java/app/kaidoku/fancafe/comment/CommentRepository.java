package app.kaidoku.fancafe.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 글의 댓글 목록. 작성자 fetch join(N+1 회피), 오래된 순. */
    @Query("""
            select c from Comment c
            join fetch c.author
            where c.post.id = :postId and c.status = :status
            order by c.createdAt asc
            """)
    List<Comment> findForPost(@Param("postId") Long postId, @Param("status") CommentStatus status);
}
