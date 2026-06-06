package app.kaidoku.fancafe.like;

import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.like.dto.LikeResponse;
import app.kaidoku.fancafe.member.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /** 좋아요 현황(공개, 로그인 시 본인 여부 포함). */
    @GetMapping("/posts/{postId}/like")
    public LikeResponse status(@PathVariable Long postId,
                               @CurrentMember(required = false) Member member) {
        return likeService.getStatus(postId, member);
    }

    /** 좋아요 토글(로그인 회원). */
    @PostMapping("/posts/{postId}/like")
    public LikeResponse toggle(@PathVariable Long postId, @CurrentMember Member member) {
        return likeService.toggle(postId, member);
    }
}
