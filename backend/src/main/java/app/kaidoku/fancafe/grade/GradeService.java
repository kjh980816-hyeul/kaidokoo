package app.kaidoku.fancafe.grade;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.grade.dto.GradeCreateRequest;
import app.kaidoku.fancafe.grade.dto.GradeResponse;
import app.kaidoku.fancafe.grade.dto.GradeUpdateRequest;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 등급(별명/칭호) 관리. 권한 검증(ADMIN)은 컨트롤러에서 수행하고, 여기선 도메인 규칙만 강제한다.
 * 등급 삭제 시 그 등급을 쓰던 회원은 기본 등급(없으면 미배정)으로 안전 이전한다(고아 FK 금지).
 */
@Service
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final MemberRepository memberRepository;

    public GradeService(GradeRepository gradeRepository, MemberRepository memberRepository) {
        this.gradeRepository = gradeRepository;
        this.memberRepository = memberRepository;
    }

    public List<GradeResponse> listGrades() {
        return gradeRepository.findAllByOrderBySortOrderAsc().stream()
                .map(GradeResponse::from)
                .toList();
    }

    @Transactional
    public Long create(GradeCreateRequest request) {
        if (gradeRepository.existsByName(request.name())) {
            throw ApiException.conflict("이미 존재하는 등급명입니다: " + request.name());
        }
        Grade grade = Grade.create(request.name(), request.sortOrder(), request.badgeColor(), request.isDefault());
        Grade saved = gradeRepository.save(grade);
        if (request.isDefault()) {
            clearOtherDefaults(saved.getId());
        }
        return saved.getId();
    }

    @Transactional
    public void update(Long gradeId, GradeUpdateRequest request) {
        Grade grade = getGrade(gradeId);
        if (!grade.getName().equals(request.name()) && gradeRepository.existsByName(request.name())) {
            throw ApiException.conflict("이미 존재하는 등급명입니다: " + request.name());
        }
        grade.update(request.name(), request.sortOrder(), request.badgeColor());
        grade.markDefault(request.isDefault());
        if (request.isDefault()) {
            clearOtherDefaults(grade.getId());
        }
    }

    @Transactional
    public void delete(Long gradeId) {
        Grade grade = getGrade(gradeId);
        Grade fallback = gradeRepository.findFirstByIsDefaultTrue()
                .filter(d -> !d.getId().equals(gradeId))
                .orElse(null);
        for (Member member : memberRepository.findByGrade_Id(gradeId)) {
            member.assignGrade(fallback);
        }
        gradeRepository.delete(grade);
    }

    private Grade getGrade(Long gradeId) {
        return gradeRepository.findById(gradeId)
                .orElseThrow(() -> ApiException.notFound("등급을 찾을 수 없습니다: " + gradeId));
    }

    /** 지정 등급을 제외한 모든 등급의 기본 플래그 해제(기본 등급 단일 보장). */
    private void clearOtherDefaults(Long keepDefaultId) {
        gradeRepository.findAll().stream()
                .filter(g -> g.isDefault() && !g.getId().equals(keepDefaultId))
                .forEach(g -> g.markDefault(false));
    }
}
