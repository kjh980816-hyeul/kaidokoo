package app.kaidoku.fancafe.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface MemberSessionRepository extends JpaRepository<MemberSession, String> {

    /** 만료 세션 일괄 정리(스케줄 작업용). */
    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime cutoff);
}
