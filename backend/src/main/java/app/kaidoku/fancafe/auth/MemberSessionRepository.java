package app.kaidoku.fancafe.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberSessionRepository extends JpaRepository<MemberSession, String> {

    /**
     * 세션 + 회원(+등급)을 한 번에 로딩한다.
     * 리졸버가 트랜잭션/세션 밖에서 member·grade를 읽으므로(open-in-view=false)
     * fetch join으로 즉시 초기화해 LazyInitializationException을 막는다.
     */
    @Query("select s from MemberSession s join fetch s.member m left join fetch m.grade where s.token = :token")
    Optional<MemberSession> findWithMemberByToken(@Param("token") String token);

    /** 만료 세션 일괄 정리(스케줄 작업용). */
    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime cutoff);
}
