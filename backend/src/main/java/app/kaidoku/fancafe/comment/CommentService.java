package app.kaidoku.fancafe.comment;

import app.kaidoku.fancafe.comment.dto.CommentCreateRequest;
import app.kaidoku.fancafe.comment.dto.CommentResponse;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.Post;
import app.kaidoku.fancafe.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 댓글 목록/작성/소프트삭제. 권한(본인·관리자)은 서버에서 재검증한다. */
@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public List<CommentResponse> listForPost(Long postId) {
        Post post = getActivePost(postId);
        return commentRepository.findVisibleForPost(post.getId()).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public Long create(Long postId, Member author, CommentCreateRequest request) {
        Post post = getActivePost(postId);
        Long parentId = resolveParentId(post.getId(), request.parentId());
        Comment comment = Comment.create(post, author, parentId, request.content().trim());
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void delete(Long commentId, Member requester) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("댓글을 찾을 수 없습니다: " + commentId));
        boolean isAuthor = comment.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("본인 또는 관리자만 댓글을 삭제할 수 있습니다.");
        }
        comment.softDelete();
    }

    private Post getActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        return post;
    }

    /** 대댓글 부모 검증: 존재하고 같은 글의 댓글이어야 한다. 최상위면 null. */
    private Long resolveParentId(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> ApiException.badRequest("상위 댓글을 찾을 수 없습니다: " + parentId));
        if (!parent.getPost().getId().equals(postId)) {
            throw ApiException.badRequest("상위 댓글이 이 글의 댓글이 아닙니다.");
        }
        return parentId;
    }
}
