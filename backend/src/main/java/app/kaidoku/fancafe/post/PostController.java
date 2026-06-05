package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import app.kaidoku.fancafe.post.dto.PostSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /** 게시판별 글 목록. */
    @GetMapping("/boards/{boardCode}/posts")
    public List<PostSummaryResponse> listByBoard(@PathVariable String boardCode) {
        return postService.listByBoard(boardCode);
    }

    /** 글 상세. */
    @GetMapping("/posts/{id}")
    public PostDetailResponse detail(@PathVariable Long id) {
        return postService.getDetail(id);
    }

    /** 글 작성. */
    @PostMapping("/posts")
    public ResponseEntity<Map<String, Long>> create(@Valid @RequestBody PostCreateRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        Long id = postService.create(request);
        URI location = uriBuilder.path("/api/posts/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(Map.of("id", id));
    }
}
