package app.kaidoku.fancafe.live;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 라이브 배너 상태(단일 행). 현재는 관리자 수동 오버라이드만 사용한다. */
@Entity
@Table(name = "live_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** (향후) 씨미 폴링 결과 캐시. 지금은 오버라이드가 항상 우선이라 표시에 직접 쓰이지 않는다. */
    @Column(name = "is_live", nullable = false)
    private boolean live;

    @Column(length = 200)
    private String title;

    @Column(name = "stream_url", length = 500)
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_mode", nullable = false, length = 20)
    private LiveOverrideMode overrideMode;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 관리자 설정. AUTO면 씨미 폴링 결과를 따르고, FORCE_*는 수동 오버라이드. */
    public void applySetting(LiveOverrideMode mode, String title, String streamUrl) {
        this.overrideMode = mode;
        this.title = title;
        this.streamUrl = streamUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /** 씨미 폴링 결과 반영(AUTO 모드의 source of truth). 제목은 방송 제목이 있을 때만 갱신. */
    public void applyPoll(boolean live, String polledTitle) {
        this.live = live;
        if (polledTitle != null && !polledTitle.isBlank()) {
            this.title = polledTitle;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** 표시용 실제 라이브 여부. 오버라이드가 폴링(live)보다 우선. */
    public boolean isEffectiveLive() {
        return switch (overrideMode) {
            case FORCE_ON -> true;
            case FORCE_OFF -> false;
            case AUTO -> live;
        };
    }
}
