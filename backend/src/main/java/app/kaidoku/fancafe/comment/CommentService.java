package app.kaidoku.fancafe.comment;

import app.kaidoku.fancafe.comment.dto.CommentCreateRequest;
import app.kaidoku.fancafe.comment.dto.CommentResponse;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.like.dto.LikeResponse;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.Post;
import app.kaidoku.fancafe.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 댓글 목록/작성/삭제(하드 삭제) + 댓글 좋아요. 권한(본인·관리자)은 서버에서 재검증한다. */
@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentLikeRepository commentLikeRepository,
                          PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.postRepository = postRepository;
    }

    /** 글의 댓글 목록. 좋아요 수·내 좋아요 여부를 일괄 계산해 채운다(viewer=null이면 liked=false).
     *  비밀댓글은 작성자 본인·운영자에게만 노출한다. */
    public List<CommentResponse> listForPost(Long postId, Member viewer) {
        Post post = getActivePost(postId);
        List<Comment> comments = commentRepository.findVisibleForPost(post.getId()).stream()
                .filter(c -> canView(c, viewer))
                .toList();
        List<Long> ids = comments.stream().map(Comment::getId).toList();

        Map<Long, Long> counts = ids.isEmpty()
                ? Map.of()
                : commentLikeRepository.countByCommentIds(ids).stream()
                        .collect(Collectors.toMap(
                                CommentLikeRepository.CommentLikeCount::getCommentId,
                                CommentLikeRepository.CommentLikeCount::getCount));
        Set<Long> likedIds = (viewer != null && !ids.isEmpty())
                ? new HashSet<>(commentLikeRepository.findLikedCommentIds(viewer.getId(), ids))
                : Set.of();

        return comments.stream()
                .map(c -> CommentResponse.from(c, counts.getOrDefault(c.getId(), 0L), likedIds.contains(c.getId())))
                .toList();
    }

    @Transactional
    public Long create(Long postId, Member author, CommentCreateRequest request) {
        Post post = getActivePost(postId);
        Long parentId = resolveParentId(post.getId(), request.parentId());
        Comment comment = Comment.create(post, author, parentId, request.content().trim());
        comment.applySecret(request.secret());
        return commentRepository.save(comment).getId();
    }

    /** 비밀댓글 열람 권한: 비밀댓글이 아니면 누구나, 비밀댓글이면 작성자 본인 또는 운영자(ADMIN)만. */
    private boolean canView(Comment comment, Member viewer) {
        if (!comment.isSecret()) {
            return true;
        }
        if (viewer == null) {
            return false;
        }
        return viewer.getRole() == Role.ADMIN || comment.getAuthor().getId().equals(viewer.getId());
    }

    /** 댓글 삭제 — 하드 삭제(행 제거). 대댓글까지 함께 삭제하고, 좋아요는 FK ON DELETE CASCADE로 정리된다. */
    @Transactional
    public void delete(Long commentId, Member requester) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("댓글을 찾을 수 없습니다: " + commentId));
        boolean isAuthor = comment.getAuthor().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("본인 또는 관리자만 댓글을 삭제할 수 있습니다.");
        }
        commentRepository.deleteByParentId(comment.getId()); // 대댓글 먼저(자식 FK)
        commentRepository.delete(comment);
    }

    /** 댓글 좋아요 토글. 이미 눌렀으면 취소, 아니면 추가. */
    @Transactional
    public LikeResponse toggleLike(Long commentId, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("댓글을 찾을 수 없습니다: " + commentId));
        boolean liked = commentLikeRepository.findByCommentIdAndMemberId(comment.getId(), member.getId())
                .map(existing -> {
                    commentLikeRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    commentLikeRepository.save(CommentLike.create(comment.getId(), member.getId()));
                    return true;
                });
        long count = commentLikeRepository.countByCommentId(comment.getId());
        return new LikeResponse(liked, (int) count);
    }

    private Post getActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        return post;
    }

    /** 대댓글 부모 검증: 존재하고, 같은 글의 댓글이며, 삭제되지 않았어야 한다. 최상위면 null. */
    private Long resolveParentId(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> ApiException.badRequest("상위 댓글을 찾을 수 없습니다: " + parentId));
        if (!parent.getPost().getId().equals(postId)) {
            throw ApiException.badRequest("상위 댓글이 이 글의 댓글이 아닙니다.");
        }
        if (parent.isDeleted()) {
            throw ApiException.badRequest("삭제된 댓글에는 답글을 달 수 없습니다.");
        }
        return parentId;
    }
}
