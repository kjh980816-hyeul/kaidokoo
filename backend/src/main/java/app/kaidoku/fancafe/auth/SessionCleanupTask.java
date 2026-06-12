package app.kaidoku.fancafe.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 만료 세션 정리(매일 새벽). 만료 세션은 리졸버에서 즉시 거부되므로 지연 정리로 충분. */
@Component
public class SessionCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupTask.class);

    private final MemberSessionRepository sessionRepository;

    public SessionCleanupTask(MemberSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void purgeExpired() {
        int removed = sessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (removed > 0) {
            log.info("만료 세션 {}건 정리", removed);
        }
    }
}
