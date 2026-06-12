package app.kaidoku.fancafe.auth;

import app.kaidoku.fancafe.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 자체 세션(ADR-0004). 토큰은 서버 생성 난수, HttpOnly 쿠키로만 전달된다. */
@Entity
@Table(name = "member_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSession {

    @Id
    @Column(length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public static MemberSession create(String token, Member member, LocalDateTime expiresAt) {
        MemberSession s = new MemberSession();
        s.token = token;
        s.member = member;
        s.createdAt = LocalDateTime.now();
        s.expiresAt = expiresAt;
        return s;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
