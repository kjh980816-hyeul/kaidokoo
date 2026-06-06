package app.kaidoku.fancafe.member.dto;

/** 회원 등급 배정(관리자). gradeId가 null이면 등급 해제. */
public record AssignGradeRequest(Long gradeId) {
}
