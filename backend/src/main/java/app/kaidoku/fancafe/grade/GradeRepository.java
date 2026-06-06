package app.kaidoku.fancafe.grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByOrderBySortOrderAsc();

    Optional<Grade> findFirstByIsDefaultTrue();

    boolean existsByName(String name);
}
