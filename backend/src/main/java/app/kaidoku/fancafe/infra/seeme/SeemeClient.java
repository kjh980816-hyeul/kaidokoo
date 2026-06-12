package app.kaidoku.fancafe.infra.seeme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 씨미(CIME) OpenAPI 어댑터 — 공개 라이브 상태 조회 전용(10-seeme).
 * Client-Id/Secret은 서버 전용(HARD). 베타 API라 응답 래핑(content) 유무에 모두 대응한다.
 * ⚠️ 엔드포인트는 developers.ci.me 기준. 실호출 실패 시 base-url(SEEME_BASE_URL)을 재확인할 것.
 */
@Component
public class SeemeClient {

    private static final Logger log = LoggerFactory.getLogger(SeemeClient.class);

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;

    public SeemeClient(ObjectMapper objectMapper,
                       @Value("${app.seeme.base-url:https://ci.me}") String baseUrl,
                       @Value("${app.seeme.client-id:}") String clientId,
                       @Value("${app.seeme.client-secret:}") String clientSecret) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    /**
     * 채널 라이브 상태. 실패하면 null(호출부는 이전 캐시 유지 — 외부 장애가 사이트를 막지 않게).
     */
    public SeemeLive fetchLiveStatus(String channelId) {
        try {
            String body = restClient.get()
                    .uri(baseUrl + "/api/openapi/v1/{channelId}/live-status", channelId)
                    .header("Client-Id", clientId)
                    .header("Client-Secret", clientSecret)
                    .retrieve()
                    .body(String.class);
            if (body == null) {
                log.warn("씨미 라이브 상태 응답이 비어 있음: channelId={}", channelId);
                return null;
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode node = root.hasNonNull("content") ? root.get("content") : root;
            if (!node.has("isLive")) {
                log.warn("씨미 라이브 상태 응답에 isLive 없음: {}", node);
                return null;
            }
            String title = node.hasNonNull("title") ? node.get("title").asText() : null;
            return new SeemeLive(node.get("isLive").asBoolean(), title);
        } catch (Exception e) {
            log.warn("씨미 라이브 상태 조회 실패: channelId={}, cause={}", channelId, e.getMessage());
            return null;
        }
    }

    public record SeemeLive(boolean live, String title) {
    }
}
