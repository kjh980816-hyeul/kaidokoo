package app.kaidoku.fancafe.grade;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.grade.dto.GradeCreateRequest;
import app.kaidoku.fancafe.member.Member;
import app.kaidoku.fancafe.member.MemberRepository;
import app.kaidoku.fancafe.member.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock GradeRepository gradeRepository;
    @Mock MemberRepository memberRepository;

    @InjectMocks GradeService gradeService;

    private Grade grade(long id, String name, boolean isDefault) {
        Grade g = Grade.create(name, 1, "#fff", isDefault);
        ReflectionTestUtils.setField(g, "id", id);
        return g;
    }

    @Test
    void create_throwsOnDuplicateName() {
        when(gradeRepository.existsByName("항해사")).thenReturn(true);

        assertThatThrownBy(() -> gradeService.create(new GradeCreateRequest("항해사", 1, "#fff", false)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("이미 존재");
    }

    @Test
    void create_clearsOtherDefaultsWhenNewIsDefault() {
        Grade existingDefault = grade(2L, "기존기본", true);
        Grade saved = grade(1L, "새기본", true);
        when(gradeRepository.existsByName("새기본")).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenReturn(saved);
        when(gradeRepository.findAll()).thenReturn(List.of(existingDefault, saved));

        gradeService.create(new GradeCreateRequest("새기본", 1, "#fff", true));

        assertThat(existingDefault.isDefault()).isFalse();
        assertThat(saved.isDefault()).isTrue();
    }

    @Test
    void delete_reassignsMembersToDefaultGrade() {
        Grade target = grade(1L, "삭제대상", false);
        Grade fallback = grade(2L, "기본", true);
        Member member = Member.create(Provider.GOOGLE, "u1", "선원");
        member.assignGrade(target);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(target));
        when(gradeRepository.findFirstByIsDefaultTrue()).thenReturn(Optional.of(fallback));
        when(memberRepository.findByGrade_Id(1L)).thenReturn(List.of(member));

        gradeService.delete(1L);

        assertThat(member.getGrade()).isEqualTo(fallback);
    }
}
