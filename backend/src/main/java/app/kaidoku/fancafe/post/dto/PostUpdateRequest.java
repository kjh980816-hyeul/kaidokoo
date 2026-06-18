package app.kaidoku.fancafe.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 글 수정 요청. 게시판(boardCode)은 바꾸지 않는다. imageUrls는 교체(전체 대체). */
public record PostUpdateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "본문은 필수입니다")
        String content,

        /** 첨부 이미지 URL 목록(교체). 비우면 이미지 제거. */
        List<String> imageUrls
) {
}
