package app.kaidoku.fancafe.common.html;

import app.kaidoku.fancafe.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HtmlSanitizer XSS 방어 단위 테스트(보안 핵심). */
class HtmlSanitizerTest {

    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void removesScriptTag() {
        String out = sanitizer.sanitize("<p>안녕</p><script>alert(1)</script>");
        assertThat(out).doesNotContain("<script");
        assertThat(out).doesNotContain("alert(1)");
        assertThat(out).contains("안녕");
    }

    @Test
    void stripsEventHandlerAttributes() {
        String out = sanitizer.sanitize(
                "<img src=\"https://x.test/a.png\" onerror=\"alert(1)\">"
                        + "<a href=\"https://x.test\" onclick=\"steal()\">link</a>");
        assertThat(out).doesNotContain("onerror");
        assertThat(out).doesNotContain("onclick");
        assertThat(out).doesNotContain("alert(1)");
    }

    @Test
    void dropsJavascriptHref() {
        String out = sanitizer.sanitize("<a href=\"javascript:alert(1)\">x</a>");
        assertThat(out).doesNotContain("javascript:");
    }

    @Test
    void removesNonYoutubeIframe() {
        String out = sanitizer.sanitize(
                "<iframe src=\"https://evil.test/embed\"></iframe>");
        assertThat(out).doesNotContain("<iframe");
        assertThat(out).doesNotContain("evil.test");
    }

    @Test
    void keepsYoutubeIframe() {
        String out = sanitizer.sanitize(
                "<iframe src=\"https://www.youtube.com/embed/abc123\" "
                        + "allowfullscreen></iframe>");
        assertThat(out).contains("<iframe");
        assertThat(out).contains("youtube.com/embed/abc123");
    }

    @Test
    void keepsAllowedStylePropsAndStripsDisallowed() {
        String out = sanitizer.sanitize(
                "<span style=\"color:red;font-size:20px;position:fixed\">x</span>");
        assertThat(out).contains("color");
        assertThat(out).contains("red");
        assertThat(out).contains("font-size");
        assertThat(out).contains("20px");
        assertThat(out).doesNotContain("position");
        assertThat(out).doesNotContain("fixed");
    }

    @Test
    void keepsBoldHeadingAndList() {
        String out = sanitizer.sanitize(
                "<h1>제목</h1><strong>굵게</strong><ul><li>항목</li></ul>");
        assertThat(out).contains("<h1>");
        assertThat(out).contains("<strong>");
        assertThat(out).contains("<ul>");
        assertThat(out).contains("<li>");
    }

    @Test
    void forcesSafeTargetAndRelOnAnchors() {
        String out = sanitizer.sanitize("<a href=\"https://x.test\">link</a>");
        assertThat(out).contains("target=\"_blank\"");
        assertThat(out).contains("rel=\"noopener nofollow ugc\"");
    }

    @Test
    void preservesBlankParagraphsAsLineBreaks() {
        // TipTap은 빈 줄(엔터)을 내용 없는 <p></p>로 내보낸다.
        // Jsoup Cleaner가 빈 블록을 지워버리면 줄바꿈이 사라지므로 <br>로 보존해야 한다.
        String out = sanitizer.sanitize("<p>첫 줄</p><p></p><p></p><p>둘째 줄</p>");
        assertThat(out).contains("첫 줄");
        assertThat(out).contains("둘째 줄");
        int brCount = out.split("<br", -1).length - 1;
        assertThat(brCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    void preservesLegacyPlainText() {
        String out = sanitizer.sanitize("그냥 평문입니다. 태그 없음.");
        assertThat(out).contains("그냥 평문입니다. 태그 없음.");
    }

    @Test
    void nullReturnsNullBlankReturnsEmpty() {
        assertThat(sanitizer.sanitize(null)).isNull();
        assertThat(sanitizer.sanitize("   ")).isEmpty();
    }

    @Test
    void rejectsOverlongContentWithBadRequest() {
        String huge = "<p>" + "a".repeat(200_001) + "</p>";
        assertThatThrownBy(() -> sanitizer.sanitize(huge))
                .isInstanceOf(ApiException.class);
    }
}
