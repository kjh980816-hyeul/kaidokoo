package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.member.Member;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 게시글. 연관관계는 LAZY, 변경은 의미 있는 메서드로(setter 미사용). */
@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    /** 말머리(선택). 게시판의 categories 중 하나이거나 null(서버 검증은 PostService). */
    @Column(length = 50)
    private String category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 첨부 이미지(표시 순서대로). Post와 생명주기를 함께한다(cascade + orphanRemoval). */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<PostImage> images = new ArrayList<>();

    public static Post create(Board board, Member author, String title, String content, String category) {
        Post p = new Post();
        p.board = board;
        p.author = author;
        p.title = title;
        p.content = content;
        p.category = category;
        p.viewCount = 0;
        p.likeCount = 0;
        p.status = PostStatus.PUBLISHED;
        p.pinned = false;
        LocalDateTime now = LocalDateTime.now();
        p.createdAt = now;
        p.updatedAt = now;
        return p;
    }

    public boolean isDeleted() {
        return this.status == PostStatus.DELETED;
    }

    /** 첨부 이미지 추가(작성 시). */
    public void addImage(String url, int sortOrder) {
        this.images.add(PostImage.of(this, url, sortOrder));
    }

    /** 제목·본문·말머리 수정(작성자/관리자). */
    public void edit(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    /** 고정 여부 설정(관리자 전용 — 권한 검증은 PostService에서). */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /** 첨부 이미지 전체 제거(수정 시 교체용 — orphanRemoval로 DB 행도 삭제). */
    public void clearImages() {
        this.images.clear();
    }

    /** 소프트 삭제(상태만 DELETED로). 첨부 파일·레코드는 보존한다. */
    public void softDelete() {
        this.status = PostStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
}
