package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardService;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.common.html.HtmlSanitizer;
import app.kaidoku.fancafe.infra.storage.FileStorageService;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostUpdateRequest;
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
    private final HtmlSanitizer htmlSanitizer;

    public PostService(PostRepository postRepository, PostImageRepository postImageRepository,
                       BoardService boardService, FileStorageService fileStorage,
                       HtmlSanitizer htmlSanitizer) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.boardService = boardService;
        this.fileStorage = fileStorage;
        this.htmlSanitizer = htmlSanitizer;
    }

    /** 특정 게시판의 공개 글 목록(대표 썸네일 포함). 비밀글은 작성자 본인·운영자에게만 노출. */
    public List<PostSummaryResponse> listByBoard(String boardCode, Member viewer) {
        Board board = boardService.getVisibleByCode(boardCode);
        List<Post> posts = postRepository.findForBoard(board.getId(), PostStatus.PUBLISHED).stream()
                .filter(p -> canView(p, viewer))
                .toList();
        if (posts.isEmpty()) {
            return List.of();
        }
        Map<Long, String> thumbs = loadThumbnails(posts.stream().map(Post::getId).toList());
        return posts.stream()
                .map(p -> PostSummaryResponse.from(p, thumbs.get(p.getId())))
                .toList();
    }

    /** 비밀글 열람 권한: 비밀글이 아니면 누구나, 비밀글이면 작성자 본인 또는 운영자(ADMIN)만. */
    private boolean canView(Post post, Member viewer) {
        if (!post.isSecret()) {
            return true;
        }
        if (viewer == null) {
            return false;
        }
        return viewer.getRole() == Role.ADMIN || post.getAuthor().getId().equals(viewer.getId());
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

    /** 글 상세 조회 + 조회수 증가. 삭제글은 404. 비밀글은 작성자 본인·운영자만 열람 가능. */
    @Transactional
    public PostDetailResponse getDetail(Long postId, Member viewer) {
        Post post = postRepository.findDetailById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        if (!canView(post, viewer)) {
            throw ApiException.forbidden("비밀글입니다. 작성자와 운영자만 볼 수 있습니다.");
        }
        // 조회수는 DB에서 원자적으로 증가(동시 조회 시 유실·다른 컬럼 덮어쓰기 방지).
        postRepository.incrementViewCount(postId);
        return PostDetailResponse.from(post, post.getViewCount() + 1);
    }

    /** 글 작성. 게시판 노출 검증 + 말머리 검증. 작성자는 컨트롤러의 @CurrentMember에서 전달된다. */
    @Transactional
    public Long create(PostCreateRequest request, Member author) {
        Board board = boardService.getVisibleByCode(request.boardCode());
        String category = validateCategory(board, request.category());
        // 본문은 클라이언트 신뢰 금지: 저장 전 서버에서 HTML 정화(XSS 방지). 제목은 평문 유지.
        String content = htmlSanitizer.sanitize(request.content());
        Post post = Post.create(board, author, request.title(), content, category);
        // 고정공지는 ADMIN만 가능. 일반 회원은 무조건 false(클라이언트 신뢰 금지).
        if (request.pinned() && author.getRole() == Role.ADMIN) {
            post.setPinned(true);
        }
        post.applySecret(request.secret());
        attachImages(post, request.imageUrls());
        return postRepository.save(post).getId();
    }

    /**
     * 말머리 검증: 비어있으면 null. 비어있지 않으면 게시판의 말머리 목록 중 하나여야 한다.
     * 게시판에 말머리가 없으면 어떤 값도 거부한다.
     */
    private String validateCategory(Board board, String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String trimmed = category.trim();
        List<String> allowed = board.categoryList();
        if (!allowed.contains(trimmed)) {
            throw ApiException.badRequest("이 게시판에서 사용할 수 없는 말머리입니다: " + trimmed);
        }
        return trimmed;
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

    /** 글 수정(작성자 본인 또는 ADMIN). 제목·본문·말머리 변경 + 이미지 전체 교체. 고정은 ADMIN만. */
    @Transactional
    public void update(Long postId, PostUpdateRequest request, Member member) {
        Post post = loadEditable(postId, member);
        String category = validateCategory(post.getBoard(), request.category());
        // 수정 시에도 동일하게 본문 정화 후 반영(XSS 방지).
        String content = htmlSanitizer.sanitize(request.content());
        post.edit(request.title(), content, category);
        // 고정공지 플래그는 ADMIN만 변경 가능. 작성자(비관리자)는 기존 상태를 건드리지 못한다.
        if (member.getRole() == Role.ADMIN) {
            post.setPinned(request.pinned());
        }
        post.applySecret(request.secret());
        post.clearImages();
        attachImages(post, request.imageUrls());
    }

    /** 글 삭제(소프트). 작성자 본인 또는 ADMIN만. 권한은 여기(서버)에서 재검증한다. */
    @Transactional
    public void delete(Long postId, Member member) {
        Post post = loadEditable(postId, member);
        post.softDelete();
    }

    /** 수정·삭제 공통: 존재·미삭제 확인 + 작성자/ADMIN 권한 재검증. */
    private Post loadEditable(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        boolean isAuthor = post.getAuthor().getId().equals(member.getId());
        boolean isAdmin = member.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("이 글에 대한 권한이 없습니다.");
        }
        return post;
    }
}
