package app.kaidoku.fancafe.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 글 작성 요청.
 * TODO(P2-auth): authorId는 임시 필드. 인증 도입 후 세션에서 작성자를 도출하고 제거한다.
 */
public record PostCreateRequest(
        @NotBlank(message = "게시판 코드는 필수입니다")
        String boardCode,

        @NotNull(message = "작성자 식별자는 필수입니다")
        Long authorId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "본문은 필수입니다")
        String content
) {
}
