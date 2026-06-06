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

    /** 관리자 수동 설정. on=true면 강제 ON(제목/링크 노출), false면 강제 OFF. */
    public void applyManual(boolean on, String title, String streamUrl) {
        this.overrideMode = on ? LiveOverrideMode.FORCE_ON : LiveOverrideMode.FORCE_OFF;
        this.title = title;
        this.streamUrl = streamUrl;
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
