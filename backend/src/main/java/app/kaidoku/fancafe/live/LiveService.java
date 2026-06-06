package app.kaidoku.fancafe.live;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.live.dto.LiveStatusResponse;
import app.kaidoku.fancafe.live.dto.LiveUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 라이브 배너 상태 조회/설정. 외부(씨미) 의존 없이 관리자 수동 오버라이드만 다룬다. */
@Service
@Transactional(readOnly = true)
public class LiveService {

    private final LiveStatusRepository liveStatusRepository;

    public LiveService(LiveStatusRepository liveStatusRepository) {
        this.liveStatusRepository = liveStatusRepository;
    }

    /** 공개 라이브 배너 상태. 행이 없으면 안전 폴백(OFF). */
    public LiveStatusResponse getPublicStatus() {
        return liveStatusRepository.findFirstByOrderByIdAsc()
                .map(LiveStatusResponse::from)
                .orElseGet(LiveStatusResponse::off);
    }

    /** 관리자 수동 설정(ON/OFF + 제목/링크). */
    @Transactional
    public LiveStatusResponse setManual(LiveUpdateRequest request) {
        LiveStatus status = liveStatusRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> ApiException.notFound("라이브 상태 행이 없습니다. 마이그레이션(V5)을 확인하세요."));
        status.applyManual(request.live(), request.title(), request.streamUrl());
        return LiveStatusResponse.from(status);
    }
}
