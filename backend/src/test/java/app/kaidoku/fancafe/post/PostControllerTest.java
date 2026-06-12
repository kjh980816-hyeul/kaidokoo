package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.auth.CurrentMemberArgumentResolver;
import app.kaidoku.fancafe.auth.MemberSession;
import app.kaidoku.fancafe.auth.MemberSessionRepository;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.Provider;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(CurrentMemberArgumentResolver.class) // 전역 WebMvcConfig가 슬라이스에서 요구하는 @CurrentMember 리졸버
class PostControllerTest {

    private static final String SESSION_COOKIE = "KAIDOKU_SESSION";

    @Autowired MockMvc mockMvc;

    @MockitoBean PostService postService;

    /** @CurrentMember 리졸버가 세션 해석에 사용(ADR-0004). */
    @MockitoBean MemberSessionRepository memberSessionRepository;

    @Test
    void detail_returnsJson() throws Exception {
        when(postService.getDetail(eq(1L))).thenReturn(new PostDetailResponse(
                1L, "free", "정박지", "제목", "본문", "선원",
                3, 0, false, LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.authorNickname").value("선원"))
                .andExpect(jsonPath("$.viewCount").value(3));
    }

    @Test
    void detail_returns404WhenMissing() throws Exception {
        when(postService.getDetail(eq(99L))).thenThrow(ApiException.notFound("글을 찾을 수 없습니다: 99"));

        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_returns400WhenTitleBlank() throws Exception {
        Member member = Member.create(Provider.GOOGLE, "u1", "선원");
        when(memberSessionRepository.findById("t1")).thenReturn(Optional.of(
                MemberSession.create("t1", member, LocalDateTime.now().plusDays(1))));
        String body = """
                {"boardCode":"free","title":"","content":"본문"}
                """;

        mockMvc.perform(post("/api/posts")
                        .cookie(new Cookie(SESSION_COOKIE, "t1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns401WithoutSession() throws Exception {
        String body = """
                {"boardCode":"free","title":"제목","content":"본문"}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns401WhenSessionExpired() throws Exception {
        Member member = Member.create(Provider.GOOGLE, "u1", "선원");
        when(memberSessionRepository.findById("old")).thenReturn(Optional.of(
                MemberSession.create("old", member, LocalDateTime.now().minusMinutes(1))));
        String body = """
                {"boardCode":"free","title":"제목","content":"본문"}
                """;

        mockMvc.perform(post("/api/posts")
                        .cookie(new Cookie(SESSION_COOKIE, "old"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
