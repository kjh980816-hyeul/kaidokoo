package app.kaidoku.fancafe.member;

import app.kaidoku.fancafe.auth.AdminGuard;
import app.kaidoku.fancafe.auth.CurrentMember;
import app.kaidoku.fancafe.member.dto.AssignGradeRequest;
import app.kaidoku.fancafe.member.dto.ChangeRoleRequest;
import app.kaidoku.fancafe.member.dto.ChangeStatusRequest;
import app.kaidoku.fancafe.member.dto.MemberAdminResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 회원 관리(관리자). 모든 진입점에서 ADMIN 서버 재검증. */
@RestController
@RequestMapping("/api/admin/members")
public class MemberAdminController {

    private final MemberService memberService;

    public MemberAdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberAdminResponse> list(@CurrentMember Member admin) {
        AdminGuard.require(admin);
        return memberService.listMembers();
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<Void> assignGrade(@CurrentMember Member admin,
                                            @PathVariable Long id,
                                            @RequestBody AssignGradeRequest request) {
        AdminGuard.require(admin);
        memberService.assignGrade(id, request.gradeId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(@CurrentMember Member admin,
                                           @PathVariable Long id,
                                           @Valid @RequestBody ChangeRoleRequest request) {
        AdminGuard.require(admin);
        memberService.changeRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@CurrentMember Member admin,
                                             @PathVariable Long id,
                                             @Valid @RequestBody ChangeStatusRequest request) {
        AdminGuard.require(admin);
        memberService.changeStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}
