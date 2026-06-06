package app.kaidoku.fancafe.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 등급 생성 요청(관리자). */
public record GradeCreateRequest(
        @NotBlank(message = "등급명은 필수입니다")
        @Size(max = 50, message = "등급명은 50자 이하여야 합니다")
        String name,

        int sortOrder,

        @Size(max = 20, message = "뱃지 색 값이 너무 깁니다")
        String badgeColor,

        boolean isDefault
) {
}
