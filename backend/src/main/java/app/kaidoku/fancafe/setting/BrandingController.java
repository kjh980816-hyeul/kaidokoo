package app.kaidoku.fancafe.setting;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.setting.dto.BrandingRequest;
import app.kaidoku.fancafe.setting.dto.BrandingResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 사이트 브랜딩(로고). 공개 조회 + 관리자 설정(ADMIN 서버 재검증). */
@RestController
public class BrandingController {

    private final BrandingService brandingService;

    public BrandingController(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    /** 공개 브랜딩 조회. 헤더 로고가 이 값을 쓴다(없으면 기본 엠블럼). */
    @GetMapping("/api/branding")
    public BrandingResponse branding() {
        return brandingService.get();
    }

    /** 로고 설정(관리자). */
    @PutMapping("/api/admin/branding")
    public BrandingResponse update(@CurrentMember Member admin,
                                   @Valid @RequestBody BrandingRequest request) {
        AdminGuard.require(admin);
        return brandingService.updateLogo(request.logoUrl());
    }
}
