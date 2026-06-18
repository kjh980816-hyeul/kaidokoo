package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardService;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.infra.storage.FileStorageService;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import app.kaidoku.fancafe.post.dto.PostSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PostService {

    /** 글 한 건당 첨부 이미지 상한(과다 업로드 방지). */
    private static final int MAX_IMAGES = 20;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final BoardService boardService;
    private final FileStorageService fileStorage;

    public PostService(PostRepository postRepository, PostImageRepository postImageRepository,
                       BoardService boardService, FileStorageService fileStorage) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.boardService = boardService;
        this.fileStorage = fileStorage;
    }

    /** 특정 게시판의 공개 글 목록(대표 썸네일 포함). */
    public List<PostSummaryResponse> listByBoard(String boardCode) {
        Board board = boardService.getVisibleByCode(boardCode);
        List<Post> posts = postRepository.findForBoard(board.getId(), PostStatus.PUBLISHED);
        if (posts.isEmpty()) {
            return List.of();
        }
        Map<Long, String> thumbs = loadThumbnails(posts.stream().map(Post::getId).toList());
        return posts.stream()
                .map(p -> PostSummaryResponse.from(p, thumbs.get(p.getId())))
                .toList();
    }

    /** 글별 대표 썸네일(가장 앞 이미지)을 한 번의 쿼리로 모은다. */
    private Map<Long, String> loadThumbnails(List<Long> postIds) {
        Map<Long, String> thumbs = new HashMap<>();
        for (Object[] row : postImageRepository.findThumbnailRows(postIds)) {
            Long postId = (Long) row[0];
            String url = (String) row[1];
            thumbs.putIfAbsent(postId, url); // 정렬상 먼저 나온 행 = 대표 이미지
        }
        return thumbs;
    }

    /** 게시글 첨부 이미지 업로드 → 저장 URL 반환(글 작성 폼에서 호출). */
    public String storeImage(MultipartFile file) {
        return fileStorage.storePostImage(file);
    }

    /** 글 상세 조회 + 조회수 증가. 삭제글은 404. */
    @Transactional
    public PostDetailResponse getDetail(Long postId) {
        Post post = postRepository.findDetailById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        // 조회수는 DB에서 원자적으로 증가(동시 조회 시 유실·다른 컬럼 덮어쓰기 방지).
        postRepository.incrementViewCount(postId);
        return PostDetailResponse.from(post, post.getViewCount() + 1);
    }

    /** 글 작성. 게시판 노출 검증. 작성자는 컨트롤러의 @CurrentMember에서 전달된다. */
    @Transactional
    public Long create(PostCreateRequest request, Member author) {
        Board board = boardService.getVisibleByCode(request.boardCode());
        Post post = Post.create(board, author, request.title(), request.content());
        attachImages(post, request.imageUrls());
        return postRepository.save(post).getId();
    }

    /** 우리가 저장한 업로드 URL만 첨부로 받아들인다(임의 외부 URL 주입 차단). 상한·빈값 정리. */
    private void attachImages(Post post, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }
        int order = 0;
        for (String url : imageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            String trimmed = url.trim();
            if (!trimmed.startsWith("/uploads/posts/")) {
                continue;
            }
            if (order >= MAX_IMAGES) {
                break;
            }
            post.addImage(trimmed, order++);
        }
    }

    /** 글 삭제(소프트). 작성자 본인 또는 ADMIN만. 권한은 여기(서버)에서 재검증한다. */
    @Transactional
    public void delete(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        boolean isAuthor = post.getAuthor().getId().equals(member.getId());
        boolean isAdmin = member.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("이 글을 삭제할 권한이 없습니다.");
        }
        post.softDelete();
    }
}
