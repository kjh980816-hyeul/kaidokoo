package app.kaidoku.fancafe.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByVisibleTrueOrderBySortOrderAsc();

    /** 관리자용: 숨김 포함 전체 게시판. */
    List<Board> findAllByOrderBySortOrderAsc();

    Optional<Board> findByCode(String code);

    boolean existsByCode(String code);
}
