package app.kaidoku.fancafe.live;

import app.kaidoku.fancafe.infra.seeme.SeemeClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 씨미 라이브 상태 주기 폴링(10-seeme). AUTO 모드 + 키/채널 설정이 있을 때만 동작.
 * 실패 시 이전 캐시 유지(SeemeClient가 null 반환) — 외부 장애 격리.
 */
@Component
public class SeemeLivePoller {

    private final SeemeClient seemeClient;
    private final LiveService liveService;

    public SeemeLivePoller(SeemeClient seemeClient, LiveService liveService) {
        this.seemeClient = seemeClient;
        this.liveService = liveService;
    }

    @Scheduled(fixedDelayString = "${app.seeme.poll-interval-ms:60000}",
            initialDelayString = "${app.seeme.poll-initial-delay-ms:15000}")
    public void poll() {
        if (!seemeClient.isConfigured()) {
            return;
        }
        String channelId = liveService.getAutoPollChannelId();
        if (channelId == null) {
            return; // AUTO 모드가 아니거나 채널 미설정
        }
        SeemeClient.SeemeLive result = seemeClient.fetchLiveStatus(channelId);
        if (result != null) {
            liveService.applyPoll(result.live(), result.title());
        }
    }
}
