package app.kaidoku.fancafe.member;

import app.kaidoku.fancafe.grade.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderUserId(Provider provider, String providerUserId);

    /** 등급 삭제 시 그 등급의 회원을 대체 등급으로 일괄 이전(단일 SQL — FK 위반 레이스 창 제거). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.grade = :fallback where m.grade.id = :gradeId")
    int reassignGrade(@Param("gradeId") Long gradeId, @Param("fallback") Grade fallback);

    /** 등급 삭제 시 대체 등급이 없으면 미배정으로 일괄 해제. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.grade = null where m.grade.id = :gradeId")
    int clearGrade(@Param("gradeId") Long gradeId);

    /** 관리자 회원 목록. 등급을 함께 로딩(N+1 회피), 최신 가입순. */
    @Query("""
            select m from Member m
            left join fetch m.grade
            order by m.createdAt desc
            """)
    List<Member> findAllWithGrade();
}
