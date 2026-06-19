package app.kaidoku.fancafe.comment;

import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.comment.dto.CommentCreateRequest;
import app.kaidoku.fancafe.comment.dto.CommentResponse;
import app.kaidoku.fancafe.like.dto.LikeResponse;
import app.kaidoku.fancafe.member.Member;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** 글의 댓글 목록(공개, 로그인 시 내 좋아요 여부 포함). */
    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> list(@PathVariable Long postId,
                                      @CurrentMember(required = false) Member viewer) {
        return commentService.listForPost(postId, viewer);
    }

    /** 댓글 좋아요 토글(로그인 회원). */
    @PostMapping("/comments/{id}/like")
    public LikeResponse toggleLike(@PathVariable Long id, @CurrentMember Member member) {
        return commentService.toggleLike(id, member);
    }

    /** 댓글 작성(로그인 회원). */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Map<String, Long>> create(@PathVariable Long postId,
                                                     @CurrentMember Member author,
                                                     @Valid @RequestBody CommentCreateRequest request) {
        Long id = commentService.create(postId, author, request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /** 댓글 삭제(작성자 본인 또는 관리자). 하드 삭제 — 대댓글·좋아요까지 함께 제거. */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @CurrentMember Member requester) {
        commentService.delete(id, requester);
        return ResponseEntity.noContent().build();
    }
}
