package app.kaidoku.fancafe.grade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByOrderBySortOrderAsc();

    Optional<Grade> findFirstByIsDefaultTrue();

    boolean existsByName(String name);

    /** 지정 등급 외 기본 플래그 일괄 해제(단일 SQL — 동시 수정 레이스 창 제거). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Grade g set g.isDefault = false where g.isDefault = true and g.id <> :keepId")
    void clearDefaultsExcept(@Param("keepId") Long keepId);
}
