package app.kaidoku.fancafe.admin;

import app.kaidoku.fancafe.admin.dto.DashboardStatsResponse;
import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 운영 대시보드(관리자). 진입 시 ADMIN 서버 재검증. */
@RestController
@RequestMapping("/api/admin/stats")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardStatsResponse stats(@CurrentMember Member admin) {
        AdminGuard.require(admin);
        return dashboardService.stats();
    }
}
