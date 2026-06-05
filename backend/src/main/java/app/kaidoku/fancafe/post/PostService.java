package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardService;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import app.kaidoku.fancafe.post.dto.PostSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;
    private final MemberRepository memberRepository;

    public PostService(PostRepository postRepository,
                       BoardService boardService,
                       MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.boardService = boardService;
        this.memberRepository = memberRepository;
    }

    /** 특정 게시판의 공개 글 목록. */
    public List<PostSummaryResponse> listByBoard(String boardCode) {
        Board board = boardService.getVisibleByCode(boardCode);
        return postRepository.findForBoard(board.getId(), PostStatus.PUBLISHED).stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    /** 글 상세 조회 + 조회수 증가. 삭제글은 404. */
    @Transactional
    public PostDetailResponse getDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("글을 찾을 수 없습니다: " + postId));
        if (post.isDeleted()) {
            throw ApiException.notFound("글을 찾을 수 없습니다: " + postId);
        }
        post.increaseViewCount();
        return PostDetailResponse.from(post);
    }

    /** 글 작성. 게시판 노출 검증 + 작성자 존재 검증. */
    @Transactional
    public Long create(PostCreateRequest request) {
        Board board = boardService.getVisibleByCode(request.boardCode());
        Member author = memberRepository.findById(request.authorId())
                .orElseThrow(() -> ApiException.badRequest("작성자를 찾을 수 없습니다: " + request.authorId()));
        Post post = Post.create(board, author, request.title(), request.content());
        return postRepository.save(post).getId();
    }
}
