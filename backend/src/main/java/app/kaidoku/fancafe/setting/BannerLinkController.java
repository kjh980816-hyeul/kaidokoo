package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.setting.dto.BannerLinksRequest;
import app.kaidoku.fancafe.setting.dto.BannerLinksResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 배너 외부 링크. 공개 조회 + 관리자 설정(ADMIN 서버 재검증). */
@RestController
public class BannerLinkController {

    private final BannerLinkService bannerLinkService;

    public BannerLinkController(BannerLinkService bannerLinkService) {
        this.bannerLinkService = bannerLinkService;
    }

    /** 공개 배너 링크 조회. 프론트 배너 버튼이 이 값으로 링크를 건다. */
    @GetMapping("/api/banner-links")
    public BannerLinksResponse links() {
        return bannerLinkService.getLinks();
    }

    /** 배너 링크 설정(관리자). 5개 키 upsert. */
    @PutMapping("/api/admin/banner-links")
    public BannerLinksResponse update(@CurrentMember Member admin,
                                      @Valid @RequestBody BannerLinksRequest request) {
        AdminGuard.require(admin);
        return bannerLinkService.updateLinks(request);
    }
}
