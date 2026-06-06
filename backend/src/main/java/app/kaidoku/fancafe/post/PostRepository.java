package app.kaidoku.fancafe.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** 게시판에 글이 하나라도 있는지(게시판 삭제 가드). */
    boolean existsByBoard_Id(Long boardId);

    /** 게시판 글 목록. 작성자 fetch join으로 N+1 회피. 고정글 우선, 최신순. */
    @Query("""
            select p from Post p
            join fetch p.author
            where p.board.id = :boardId and p.status = :status
            order by p.pinned desc, p.createdAt desc
            """)
    List<Post> findForBoard(@Param("boardId") Long boardId, @Param("status") PostStatus status);
}
