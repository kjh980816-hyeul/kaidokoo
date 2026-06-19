package app.kaidoku.fancafe.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 글 작성 요청. 작성자는 본문이 아니라 @CurrentMember(서버 신원)에서 도출한다. */
public record PostCreateRequest(
        @NotBlank(message = "게시판 코드는 필수입니다")
        String boardCode,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "본문은 필수입니다")
        String content,

        /** 말머리(선택). 게시판의 categories 중 하나여야 한다(서버 검증). null/빈값 허용. */
        @Size(max = 50, message = "말머리는 50자 이하여야 합니다")
        String category,

        /** 고정공지 여부. ADMIN만 true가 반영되고, 일반 회원은 강제로 false 처리(서버 검증). */
        boolean pinned,

        /** 첨부 이미지 URL 목록(업로드 후 받은 /uploads/posts/... 경로). 선택. */
        List<String> imageUrls
) {
}
