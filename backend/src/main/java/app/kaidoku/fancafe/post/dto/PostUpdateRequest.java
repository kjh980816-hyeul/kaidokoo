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

        /** 말머리(선택). 게시판의 categories 중 하나여야 한다(서버 검증). null/빈값 허용. */
        @Size(max = 50, message = "말머리는 50자 이하여야 합니다")
        String category,

        /** 고정공지 여부. ADMIN만 반영되고, 일반 회원(작성자 포함)은 강제로 false 처리(서버 검증). */
        boolean pinned,

        /** 비밀글 여부. true면 작성자 본인·운영자만 열람 가능. */
        boolean secret,

        /** 첨부 이미지 URL 목록(교체). 비우면 이미지 제거. */
        List<String> imageUrls
) {
}
