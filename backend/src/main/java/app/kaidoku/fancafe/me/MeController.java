package app.kaidoku.fancafe.me;

import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.auth.dto.MeResponse;
import app.kaidoku.fancafe.me.dto.UpdateNicknameRequest;
import app.kaidoku.fancafe.member.Member;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 본인 프로필 관리. 모두 로그인 필수(@CurrentMember required) — 세션 회원만 자기 정보를 바꾼다.
 * 응답은 갱신된 {@link MeResponse}로, 프론트가 헤더/마이페이지 상태를 즉시 반영한다.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    /** 닉네임 변경. */
    @PatchMapping
    public MeResponse updateNickname(@CurrentMember Member member,
                                     @RequestBody @Valid UpdateNicknameRequest request) {
        return MeResponse.of(meService.updateNickname(member.getId(), request.nickname()));
    }

    /** 프로필 사진 업로드(multipart, 필드명 file). */
    @PostMapping("/avatar")
    public MeResponse uploadAvatar(@CurrentMember Member member,
                                   @RequestParam("file") MultipartFile file) {
        return MeResponse.of(meService.updateAvatar(member.getId(), file));
    }

    /** 프로필 사진 제거(기본 아바타로 되돌림). */
    @DeleteMapping("/avatar")
    public MeResponse removeAvatar(@CurrentMember Member member) {
        return MeResponse.of(meService.removeAvatar(member.getId()));
    }
}
