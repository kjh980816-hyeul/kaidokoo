package app.kaidoku.fancafe.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** threshold 이후 작성된 공개 글이 있는 게시판 id 집합(홈 카드 NEW 뱃지용, 단일 쿼리로 N+1 회피). */
    @Query("""
            select distinct p.board.id from Post p
            where p.status = app.kaidoku.fancafe.post.PostStatus.PUBLISHED
              and p.createdAt > :threshold
            """)
    Set<Long> boardIdsWithPostsAfter(@Param("threshold") LocalDateTime threshold);

    /** 삭제(soft delete)되지 않은 글이 하나라도 있는지(게시판 삭제 가드). */
    boolean existsByBoard_IdAndStatusNot(Long boardId, PostStatus status);

    /** 상태별 글 수(대시보드 통계 — PUBLISHED만 집계). */
    long countByStatus(PostStatus status);

    /** threshold 이후 작성된 특정 상태 글 수(대시보드 신규 글 통계). */
    long countByStatusAndCreatedAtAfter(PostStatus status, LocalDateTime threshold);

    /** 게시판 글 목록. 작성자·게시판 fetch join으로 N+1/LAZY 예외 회피. 고정글 우선, 최신순. */
    @Query("""
            select p from Post p
            join fetch p.author
            join fetch p.board
            where p.board.id = :boardId and p.status = :status
            order by p.pinned desc, p.createdAt desc
            """)
    List<Post> findForBoard(@Param("boardId") Long boardId, @Param("status") PostStatus status);

    /** 글 상세. 응답 DTO가 쓰는 작성자·게시판을 함께 로딩. */
    @Query("""
            select p from Post p
            join fetch p.author
            join fetch p.board
            where p.id = :postId
            """)
    Optional<Post> findDetailById(@Param("postId") Long postId);

    /** 조회수 원자적 증가(read-modify-write 레이스로 인한 유실 방지). */
    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /** 좋아요 수 원자적 증가. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = p.likeCount + 1 where p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    /** 좋아요 수 원자적 감소(0 미만 방지). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Post p
            set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end
            where p.id = :postId
            """)
    void decrementLikeCount(@Param("postId") Long postId);
}
