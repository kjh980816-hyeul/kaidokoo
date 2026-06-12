package app.kaidoku.fancafe.live;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.live.dto.LiveStatusResponse;
import app.kaidoku.fancafe.live.dto.LiveUpdateRequest;
import app.kaidoku.fancafe.member.Member;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveController {

    private final LiveService liveService;

    public LiveController(LiveService liveService) {
        this.liveService = liveService;
    }

    /** 공개 라이브 배너 상태. 프론트는 이 엔드포인트만 호출(씨미 직접 호출·키 노출 회피). */
    @GetMapping("/api/live-status")
    public LiveStatusResponse status() {
        return liveService.getPublicStatus();
    }

    /** 라이브 배너 설정(관리자): 모드(AUTO/FORCE_*)·제목·링크·씨미 채널 ID. */
    @PutMapping("/api/admin/live-status")
    public LiveStatusResponse update(@CurrentMember Member admin,
                                     @Valid @RequestBody LiveUpdateRequest request) {
        AdminGuard.require(admin);
        return liveService.updateSetting(request);
    }
}
