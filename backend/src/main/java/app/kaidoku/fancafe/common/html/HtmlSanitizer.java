package app.kaidoku.fancafe.common.html;

import app.kaidoku.fancafe.common.ApiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * 게시글 본문(리치 텍스트) HTML을 서버에서 정화한다. XSS 방지(HARD 제약).
 *
 * <p>프론트 에디터(TipTap)와 동일한 허용 계약(allow-list)에 맞춰 화이트리스트 방식으로만 통과시킨다.
 * Jsoup {@link Safelist}로 1차 정화 후, Jsoup가 그대로 보존하는 {@code style}/{@code iframe} 등을
 * 추가로 스크럽한다(2차 하드닝).
 */
@Component
public class HtmlSanitizer {

    /** 정화 후 본문 최대 길이(문자). 초과 시 400. 매직 넘버 박제 회피용 상수. */
    private static final int MAX_CONTENT_LENGTH = 200_000;

    /** style 속성에서 살아남는 CSS 속성 화이트리스트. */
    private static final Set<String> ALLOWED_STYLE_PROPS = Set.of(
            "color", "background-color", "font-size", "font-family", "text-align",
            "font-weight", "font-style", "text-decoration");

    /** style 값에 들어 있으면 통째로 거르는 위험 토큰. */
    private static final String[] DANGEROUS_STYLE_TOKENS = {
            "javascript:", "expression(", "url(", "@import", "<"};

    /** 비어 있으면 <br>로 보존할 블록 태그(Jsoup Cleaner의 빈 블록 제거 회피). */
    private static final Set<String> BLANK_PRESERVE_TAGS = Set.of(
            "p", "h1", "h2", "h3", "li", "blockquote");

    /** iframe src 호스트 화이트리스트(영상 임베드 한정). */
    private static final Set<String> ALLOWED_IFRAME_HOSTS = Set.of(
            "youtube.com", "www.youtube.com",
            "youtube-nocookie.com", "www.youtube-nocookie.com",
            "youtu.be", "player.vimeo.com");

    private final Safelist safelist = buildSafelist();

    /**
     * 원시 HTML을 안전한 HTML로 정화한다.
     *
     * @param rawHtml 클라이언트가 보낸 원시 HTML(신뢰 불가)
     * @return 정화된 HTML. 입력이 null이면 null, blank면 빈 문자열.
     */
    public String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return null;
        }
        if (rawHtml.isBlank()) {
            return "";
        }

        // 0차: 빈 블록(<p></p> 등)에 <br> 주입. TipTap이 빈 줄(엔터)을 빈 문단으로 내보내는데
        //      Jsoup Cleaner는 내용 없는 블록을 제거해 줄바꿈이 사라진다 → 미리 살려둔다.
        String prepared = preserveBlankBlocks(rawHtml);

        // 1차: Jsoup 화이트리스트 정화. baseUri 빈 문자열 → 상대경로는 프로토콜 강제에 걸려 제거.
        String cleaned = Jsoup.clean(prepared, "", safelist);

        // 2차: Jsoup가 그대로 둔 style/iframe/a 하드닝.
        Document doc = Jsoup.parseBodyFragment(cleaned);
        scrubStyles(doc);
        enforceIframeHostAllowlist(doc);
        hardenAnchors(doc);

        String result = doc.body().html();

        if (result.length() > MAX_CONTENT_LENGTH) {
            throw ApiException.badRequest("본문이 너무 깁니다. 허용 길이를 초과했습니다.");
        }
        return result;
    }

    /**
     * 내용 없는 빈 블록 요소에 {@code <br>}를 넣어 후속 Jsoup 정화에서 살아남게 한다.
     * (TipTap은 빈 줄을 {@code <p></p>}로 출력하고, Jsoup Cleaner는 빈 블록을 제거한다.)
     */
    private String preserveBlankBlocks(String rawHtml) {
        Document doc = Jsoup.parseBodyFragment(rawHtml);
        for (Element el : doc.select(String.join(", ", BLANK_PRESERVE_TAGS))) {
            boolean hasContent = el.hasText() || !el.select("img, br, iframe").isEmpty();
            if (!hasContent) {
                el.appendChild(new Element("br"));
            }
        }
        return doc.body().html();
    }

    private Safelist buildSafelist() {
        return Safelist.none()
                .addTags("p", "br", "span", "div", "strong", "b", "em", "i", "u", "s", "strike",
                        "h1", "h2", "h3", "blockquote", "ul", "ol", "li", "a", "iframe", "img")
                .addAttributes("span", "style")
                .addAttributes("div", "style")
                .addAttributes("p", "style")
                .addAttributes("h1", "style")
                .addAttributes("h2", "style")
                .addAttributes("h3", "style")
                .addAttributes("li", "style")
                .addAttributes("blockquote", "style")
                .addAttributes("a", "href", "target", "rel")
                .addAttributes("iframe", "src", "width", "height", "frameborder", "allow", "allowfullscreen")
                .addAttributes("img", "src", "alt", "width", "height")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https")
                .addProtocols("iframe", "src", "https");
    }

    /** 모든 style 속성에서 화이트리스트 외 선언/위험 값을 제거하고 재직렬화한다. */
    private void scrubStyles(Document doc) {
        for (Element el : doc.select("[style]")) {
            String cleanedStyle = cleanStyleValue(el.attr("style"));
            if (cleanedStyle.isEmpty()) {
                el.removeAttr("style");
            } else {
                el.attr("style", cleanedStyle);
            }
        }
    }

    private String cleanStyleValue(String style) {
        StringBuilder kept = new StringBuilder();
        for (String declaration : style.split(";")) {
            String decl = declaration.trim();
            if (decl.isEmpty()) {
                continue;
            }
            int colon = decl.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String property = decl.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = decl.substring(colon + 1).trim();
            if (!ALLOWED_STYLE_PROPS.contains(property)) {
                continue;
            }
            if (value.isEmpty() || containsDangerousToken(value)) {
                continue;
            }
            kept.append(property).append(": ").append(value).append("; ");
        }
        return kept.toString().trim();
    }

    private boolean containsDangerousToken(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return Arrays.stream(DANGEROUS_STYLE_TOKENS).anyMatch(lower::contains);
    }

    /** src 호스트가 화이트리스트 밖이거나 파싱 불가하면 iframe 자체를 제거한다. */
    private void enforceIframeHostAllowlist(Document doc) {
        for (Element iframe : doc.select("iframe")) {
            if (!isAllowedIframeHost(iframe.attr("src"))) {
                iframe.remove();
            }
        }
    }

    private boolean isAllowedIframeHost(String src) {
        if (src == null || src.isBlank()) {
            return false;
        }
        try {
            String host = URI.create(src.trim()).getHost();
            return host != null && ALLOWED_IFRAME_HOSTS.contains(host.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return false; // 파싱 불가 → 제거
        }
    }

    /** 살아남은 모든 a 태그에 안전한 target/rel을 강제한다. */
    private void hardenAnchors(Document doc) {
        for (Element a : doc.select("a")) {
            a.attr("target", "_blank");
            a.attr("rel", "noopener nofollow ugc");
        }
    }
}
