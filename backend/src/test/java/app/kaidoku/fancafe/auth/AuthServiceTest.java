package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.Grade;
import app.kaidoku.fancafe.grade.GradeRepository;
import app.kaidoku.fancafe.infra.oauth.OAuthClient;
import app.kaidoku.fancafe.infra.oauth.OAuthClients;
import app.kaidoku.fancafe.infra.oauth.OAuthProfile;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock OAuthClients oauthClients;
    @Mock OAuthClient naverClient;
    @Mock MemberRepository memberRepository;
    @Mock GradeRepository gradeRepository;
    @Mock MemberSessionRepository sessionRepository;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(oauthClients, memberRepository, gradeRepository,
                sessionRepository, "http://localhost:5173");
    }

    private void stubProfile(String providerUserId, String nickname) {
        when(oauthClients.get(Provider.NAVER)).thenReturn(naverClient);
        when(naverClient.fetchProfile(eq("code-1"), anyString()))
                .thenReturn(new OAuthProfile(providerUserId, nickname));
    }

    @Test
    void firstMemberBecomesAdminWithDefaultGrade() {
        stubProfile("naver-1", "준하");
        Grade defaultGrade = Grade.create("견습 선원", 1, "#fff", true);
        when(memberRepository.findByProviderAndProviderUserId(Provider.NAVER, "naver-1"))
                .thenReturn(Optional.empty());
        when(gradeRepository.findFirstByIsDefaultTrue()).thenReturn(Optional.of(defaultGrade));
        when(memberRepository.count()).thenReturn(0L);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = authService.loginWithCode(Provider.NAVER, "code-1");

        assertThat(token).isNotBlank();
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN); // 최초 가입자 부트스트랩
        assertThat(captor.getValue().getGrade()).isEqualTo(defaultGrade);
        verify(sessionRepository).save(any(MemberSession.class));
    }

    @Test
    void laterMembersStayAsMember() {
        stubProfile("naver-2", "선원");
        when(memberRepository.findByProviderAndProviderUserId(Provider.NAVER, "naver-2"))
                .thenReturn(Optional.empty());
        when(gradeRepository.findFirstByIsDefaultTrue()).thenReturn(Optional.empty());
        when(memberRepository.count()).thenReturn(3L);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.loginWithCode(Provider.NAVER, "code-1");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    void existingMemberIsReusedWithoutSave() {
        stubProfile("naver-1", "준하");
        Member existing = Member.create(Provider.NAVER, "naver-1", "준하");
        ReflectionTestUtils.setField(existing, "id", 7L);
        when(memberRepository.findByProviderAndProviderUserId(Provider.NAVER, "naver-1"))
                .thenReturn(Optional.of(existing));

        authService.loginWithCode(Provider.NAVER, "code-1");

        verify(memberRepository, never()).save(any(Member.class));
        ArgumentCaptor<MemberSession> captor = ArgumentCaptor.forClass(MemberSession.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getMember()).isEqualTo(existing);
        assertThat(captor.getValue().isExpired()).isFalse();
    }

    @Test
    void logoutDeletesSession() {
        authService.logout("token-1");

        verify(sessionRepository).deleteById("token-1");
    }
}
