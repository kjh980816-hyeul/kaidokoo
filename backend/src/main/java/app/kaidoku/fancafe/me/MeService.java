package app.kaidoku.fancafe.me;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.infra.storage.FileStorageService;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 본인 프로필(닉네임·프로필 사진) 변경. 항상 세션 회원 id로 DB에서 다시 로딩해 수정한다
 * (리졸버가 넘긴 엔티티는 detached, 그리고 권한은 "본인만" 이므로 id 일치로 보장).
 */
@Service
@Transactional(readOnly = true)
public class MeService {

    private final MemberRepository memberRepository;
    private final FileStorageService fileStorage;

    public MeService(MemberRepository memberRepository, FileStorageService fileStorage) {
        this.memberRepository = memberRepository;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public Member updateNickname(Long memberId, String nickname) {
        String trimmed = nickname == null ? "" : nickname.strip();
        if (trimmed.length() < 2 || trimmed.length() > 20) {
            throw ApiException.badRequest("닉네임은 2~20자여야 합니다.");
        }
        Member member = load(memberId);
        member.changeNickname(trimmed);
        return member;
    }

    @Transactional
    public Member updateAvatar(Long memberId, MultipartFile file) {
        Member member = load(memberId);
        String previous = member.getAvatarUrl();
        String url = fileStorage.storeAvatar(file);
        member.changeAvatarUrl(url);
        // 새 파일 저장에 성공한 뒤에만 이전 파일 정리(실패 시 기존 프사 유지).
        fileStorage.deleteIfManaged(previous);
        return member;
    }

    @Transactional
    public Member removeAvatar(Long memberId) {
        Member member = load(memberId);
        String previous = member.getAvatarUrl();
        member.changeAvatarUrl(null);
        fileStorage.deleteIfManaged(previous);
        return member;
    }

    private Member load(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ApiException.notFound("회원을 찾을 수 없습니다."));
    }
}
