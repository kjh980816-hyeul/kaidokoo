package app.kaidoku.fancafe.stats;

import app.kaidoku.fancafe.stats.dto.PublicStatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 공개 통계(인증 불필요). 사이드바 "선원 N명" 등 비민감 수치. */
@RestController
@RequestMapping("/api/stats")
public class PublicStatsController {

    private final PublicStatsService publicStatsService;

    public PublicStatsController(PublicStatsService publicStatsService) {
        this.publicStatsService = publicStatsService;
    }

    @GetMapping
    public PublicStatsResponse stats() {
        return publicStatsService.stats();
    }
}
