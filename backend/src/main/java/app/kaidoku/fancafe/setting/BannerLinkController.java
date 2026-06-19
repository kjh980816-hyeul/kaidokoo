package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.setting.dto.BannerItem;
import app.kaidoku.fancafe.setting.dto.BannerItemsRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 배너 항목(이름+링크 자유 추가). 공개 조회 + 관리자 설정(ADMIN 서버 재검증). */
@RestController
public class BannerLinkController {

    private final BannerLinkService bannerLinkService;

    public BannerLinkController(BannerLinkService bannerLinkService) {
        this.bannerLinkService = bannerLinkService;
    }

    /** 공개 배너 항목 조회. 프론트가 이 목록으로 배너 버튼을 렌더한다. */
    @GetMapping("/api/banner-links")
    public List<BannerItem> links() {
        return bannerLinkService.getItems();
    }

    /** 배너 항목 일괄 설정(관리자). items 전체로 교체. */
    @PutMapping("/api/admin/banner-links")
    public List<BannerItem> update(@CurrentMember Member admin,
                                   @Valid @RequestBody BannerItemsRequest request) {
        AdminGuard.require(admin);
        return bannerLinkService.updateItems(request.items());
    }
}
