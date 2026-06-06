package app.kaidoku.fancafe.post;

import app.kaidoku.fancafe.auth.CurrentMemberArgumentResolver;
import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.post.dto.PostDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(CurrentMemberArgumentResolver.class) // 전역 WebMvcConfig가 슬라이스에서 요구하는 @CurrentMember 리졸버
class PostControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean PostService postService;

    /** 리졸버 의존성. PostController는 @CurrentMember를 쓰지 않지만 슬라이스 배선상 필요. */
    @MockitoBean MemberRepository memberRepository;

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
        String body = """
                {"boardCode":"free","authorId":1,"title":"","content":"본문"}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
