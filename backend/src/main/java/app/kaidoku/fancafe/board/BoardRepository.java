package app.kaidoku.fancafe.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByVisibleTrueOrderBySortOrderAsc();

    Optional<Board> findByCode(String code);
}
