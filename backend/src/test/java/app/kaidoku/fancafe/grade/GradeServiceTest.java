package app.kaidoku.fancafe.grade;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.grade.dto.GradeCreateRequest;
import app.kaidoku.fancafe.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        Grade saved = grade(1L, "새기본", true);
        when(gradeRepository.existsByName("새기본")).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenReturn(saved);

        gradeService.create(new GradeCreateRequest("새기본", 1, "#fff", true));

        verify(gradeRepository).clearDefaultsExcept(1L);
    }

    @Test
    void delete_reassignsMembersToDefaultGrade() {
        Grade target = grade(1L, "삭제대상", false);
        Grade fallback = grade(2L, "기본", true);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(target));
        when(gradeRepository.findFirstByIsDefaultTrue()).thenReturn(Optional.of(fallback));

        gradeService.delete(1L);

        verify(memberRepository).reassignGrade(1L, fallback);
        verify(gradeRepository).delete(target);
    }

    @Test
    void delete_clearsGradeWhenNoDefaultExists() {
        Grade target = grade(1L, "삭제대상", false);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(target));
        when(gradeRepository.findFirstByIsDefaultTrue()).thenReturn(Optional.empty());

        gradeService.delete(1L);

        verify(memberRepository).clearGrade(1L);
        verify(gradeRepository).delete(target);
    }

    @Test
    void delete_blockedForDefaultGrade() {
        Grade target = grade(1L, "기본등급", true);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> gradeService.delete(1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("기본 등급");
        verify(gradeRepository, never()).delete(any(Grade.class));
    }
}
