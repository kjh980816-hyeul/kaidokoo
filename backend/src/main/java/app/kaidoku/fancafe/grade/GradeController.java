package app.kaidoku.fancafe.grade;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.grade.dto.GradeCreateRequest;
import app.kaidoku.fancafe.grade.dto.GradeResponse;
import app.kaidoku.fancafe.grade.dto.GradeUpdateRequest;
import app.kaidoku.fancafe.member.Member;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    /** 공개 등급 목록(뱃지 표시·관리 화면 공용). */
    @GetMapping("/api/grades")
    public List<GradeResponse> list() {
        return gradeService.listGrades();
    }

    /** 등급 생성(관리자). */
    @PostMapping("/api/admin/grades")
    public ResponseEntity<Map<String, Long>> create(@CurrentMember Member admin,
                                                     @Valid @RequestBody GradeCreateRequest request) {
        AdminGuard.require(admin);
        Long id = gradeService.create(request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /** 등급 수정(관리자). */
    @PutMapping("/api/admin/grades/{id}")
    public ResponseEntity<Void> update(@CurrentMember Member admin,
                                       @PathVariable Long id,
                                       @Valid @RequestBody GradeUpdateRequest request) {
        AdminGuard.require(admin);
        gradeService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    /** 등급 삭제(관리자). 사용 중 회원은 기본 등급으로 이전. */
    @DeleteMapping("/api/admin/grades/{id}")
    public ResponseEntity<Void> delete(@CurrentMember Member admin, @PathVariable Long id) {
        AdminGuard.require(admin);
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
