package app.kaidoku.fancafe;

import app.kaidoku.fancafe.attendance.AttendanceService;
import app.kaidoku.fancafe.attendance.dto.AttendanceResponse;
import app.kaidoku.fancafe.board.Board;
import app.kaidoku.fancafe.board.BoardRepository;
import app.kaidoku.fancafe.board.BoardType;
import app.kaidoku.fancafe.comment.CommentService;
import app.kaidoku.fancafe.comment.dto.CommentCreateRequest;
import app.kaidoku.fancafe.comment.dto.CommentResponse;
import app.kaidoku.fancafe.common.Role;
import app.kaidoku.fancafe.grade.GradeService;
import app.kaidoku.fancafe.grade.dto.GradeCreateRequest;
import app.kaidoku.fancafe.like.LikeService;
import app.kaidoku.fancafe.live.LiveOverrideMode;
import app.kaidoku.fancafe.live.LiveService;
import app.kaidoku.fancafe.live.dto.LiveStatusResponse;
import app.kaidoku.fancafe.live.dto.LiveUpdateRequest;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.MemberService;
import app.kaidoku.fancafe.member.Provider;
import app.kaidoku.fancafe.post.PostService;
import app.kaidoku.fancafe.post.dto.PostCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P2~ 커뮤니티 기능 통합 검증: Flyway V2~V5 스키마 → JPA validate → 댓글/좋아요/출석/등급/라이브 서비스 왕복.
 * 매핑이 어긋나면 컨텍스트 로딩 또는 쿼리에서 실패하므로, 통과 = 새 슬라이스 배선 정상.
 */
@SpringBootTest
@Transactional
class CommunityIntegrationTest {

    @Autowired BoardRepository boardRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired PostService postService;
    @Autowired CommentService commentService;
    @Autowired LikeService likeService;
    @Autowired AttendanceService attendanceService;
    @Autowired GradeService gradeService;
    @Autowired MemberService memberService;
    @Autowired LiveService liveService;

    private Member newMember(String key) {
        return memberRepository.save(Member.create(Provider.GOOGLE, key, "선원" + key));
    }

    private Long newPost(Member author) {
        Board board = boardRepository.save(
                Board.create("c-" + author.getProviderUserId(), "테스트", null, null, 1,
                        BoardType.GENERAL, Role.MEMBER));
        return postService.create(new PostCreateRequest(board.getCode(), "제목", "본문"), author);
    }

    @Test
    void comment_createAndList() {
        Member author = newMember("c1");
        Long postId = newPost(author);

        commentService.create(postId, author, new CommentCreateRequest("첫 댓글", null));
        List<CommentResponse> comments = commentService.listForPost(postId);

        assertThat(comments).extracting(CommentResponse::content).contains("첫 댓글");
        assertThat(comments).extracting(CommentResponse::authorNickname).contains("선원c1");
    }

    @Test
    void like_toggleOnThenOff() {
        Member member = newMember("l1");
        Long postId = newPost(member);

        assertThat(likeService.toggle(postId, member).liked()).isTrue();
        assertThat(likeService.getStatus(postId, member).likeCount()).isEqualTo(1);
        assertThat(likeService.toggle(postId, member).liked()).isFalse();
        assertThat(likeService.getStatus(postId, member).likeCount()).isZero();
    }

    @Test
    void attendance_checkInIsIdempotent() {
        Member member = newMember("a1");

        AttendanceResponse first = attendanceService.checkIn(member);
        AttendanceResponse second = attendanceService.checkIn(member);

        assertThat(first.checkedToday()).isTrue();
        assertThat(first.streak()).isEqualTo(1);
        assertThat(second.totalDays()).isEqualTo(1); // 중복 체크인은 누적되지 않음
    }

    @Test
    void grade_createThenAssignToMember() {
        Member member = newMember("g1");
        Long gradeId = gradeService.create(new GradeCreateRequest("항해사", 3, "#D4AF6A", false));

        memberService.assignGrade(member.getId(), gradeId);

        Member reloaded = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(reloaded.getGrade()).isNotNull();
        assertThat(reloaded.getGrade().getName()).isEqualTo("항해사");
    }

    @Test
    void live_manualToggleReflectsInPublicStatus() {
        liveService.updateSetting(new LiveUpdateRequest(
                LiveOverrideMode.FORCE_ON, "오늘 별바다 항해 방송", "https://example.test/live", "@kaiijoku"));

        LiveStatusResponse on = liveService.getPublicStatus();
        assertThat(on.live()).isTrue();
        assertThat(on.title()).isEqualTo("오늘 별바다 항해 방송");
        assertThat(on.channelId()).isEqualTo("@kaiijoku");

        liveService.updateSetting(new LiveUpdateRequest(LiveOverrideMode.FORCE_OFF, null, null, null));
        assertThat(liveService.getPublicStatus().live()).isFalse();
    }

    @Test
    void live_autoModeFollowsPollResult() {
        liveService.updateSetting(new LiveUpdateRequest(LiveOverrideMode.AUTO, null, null, "@kaiijoku"));
        assertThat(liveService.getAutoPollChannelId()).isEqualTo("@kaiijoku");

        liveService.applyPoll(true, "폴링된 방송 제목");
        LiveStatusResponse on = liveService.getPublicStatus();
        assertThat(on.live()).isTrue();
        assertThat(on.title()).isEqualTo("폴링된 방송 제목");

        liveService.applyPoll(false, null);
        assertThat(liveService.getPublicStatus().live()).isFalse();
    }
}
