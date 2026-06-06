package app.kaidoku.fancafe.attendance;

import app.kaidoku.fancafe.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 출석 기록. (회원, 날짜) 유니크 — 하루 1회. 날짜는 KST 기준. */
@Entity
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "attend_date", nullable = false)
    private LocalDate attendDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Attendance create(Member member, LocalDate attendDate) {
        Attendance a = new Attendance();
        a.member = member;
        a.attendDate = attendDate;
        a.createdAt = LocalDateTime.now();
        return a;
    }
}
