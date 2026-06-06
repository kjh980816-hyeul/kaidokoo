package app.kaidoku.fancafe.member;

import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.Grade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 회원. 멀티프로바이더(provider + providerUserId)로 소셜 계정을 식별한다. */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 190)
    private String providerUserId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    /** 등급(별명/칭호). 관리자가 수동 배정. 인증 전 회원은 null일 수 있다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Member create(Provider provider, String providerUserId, String nickname) {
        Member m = new Member();
        m.provider = provider;
        m.providerUserId = providerUserId;
        m.nickname = nickname;
        m.role = Role.MEMBER;
        m.status = MemberStatus.ACTIVE;
        m.createdAt = LocalDateTime.now();
        return m;
    }

    public void assignGrade(Grade grade) {
        this.grade = grade;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}
