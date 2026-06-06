package app.kaidoku.fancafe.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderUserId(Provider provider, String providerUserId);

    /** 해당 등급을 배정받은 회원들(등급 삭제 시 이전 처리). */
    List<Member> findByGrade_Id(Long gradeId);

    /** 관리자 회원 목록. 등급을 함께 로딩(N+1 회피), 최신 가입순. */
    @Query("""
            select m from Member m
            left join fetch m.grade
            order by m.createdAt desc
            """)
    List<Member> findAllWithGrade();
}
