package app.kaidoku.fancafe.stats;

import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.MemberStatus;
import app.kaidoku.fancafe.stats.dto.PublicStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공개 통계 집계(비로그인 노출용). 현재는 활동 중인 선원 수만. */
@Service
public class PublicStatsService {

    private final MemberRepository memberRepository;

    public PublicStatsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public PublicStatsResponse stats() {
        // 정지·탈퇴를 뺀 활동 회원만 "선원"으로 노출한다.
        return new PublicStatsResponse(memberRepository.countByStatus(MemberStatus.ACTIVE));
    }
}
