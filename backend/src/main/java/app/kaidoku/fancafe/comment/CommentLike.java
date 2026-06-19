package app.kaidoku.fancafe.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 좋아요. (댓글, 회원) 유니크 — 1인 1회.
 * 댓글/글처럼 네비게이션이 필요 없어 관계 대신 단순 Long 컬럼으로 매핑(LAZY 로딩·배치 집계 용이).
 */
@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CommentLike create(Long commentId, Long memberId) {
        CommentLike like = new CommentLike();
        like.commentId = commentId;
        like.memberId = memberId;
        like.createdAt = LocalDateTime.now();
        return like;
    }
}
