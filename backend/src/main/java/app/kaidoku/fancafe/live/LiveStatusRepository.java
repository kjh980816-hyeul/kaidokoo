package app.kaidoku.fancafe.live;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LiveStatusRepository extends JpaRepository<LiveStatus, Long> {

    /** 단일 라이브 상태 행(V5에서 베이스라인 1건 시드). */
    Optional<LiveStatus> findFirstByOrderByIdAsc();
}
