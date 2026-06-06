package app.kaidoku.fancafe.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 작성 요청. 작성자는 {@code @CurrentMember}(헤더)에서 도출하므로 본문에 받지 않는다. */
public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
        String content,

        /** 대댓글이면 상위 댓글 id, 최상위면 null. */
        Long parentId
) {
}
