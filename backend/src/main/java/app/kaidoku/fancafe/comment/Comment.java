package app.kaidoku.fancafe.comment;

import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글. parentId(자기참조 FK)로 대댓글을 표현하되, 코드에선 네비게이션하지 않으므로
 * 관계 대신 단순 Long 컬럼으로 매핑(불필요한 LAZY 로딩 회피).
 */
@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Comment create(Post post, Member author, Long parentId, String content) {
        Comment c = new Comment();
        c.post = post;
        c.author = author;
        c.parentId = parentId;
        c.content = content;
        c.status = CommentStatus.PUBLISHED;
        c.createdAt = LocalDateTime.now();
        return c;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
    }

    public boolean isDeleted() {
        return this.status == CommentStatus.DELETED;
    }
}
