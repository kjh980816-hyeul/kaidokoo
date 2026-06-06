package app.kaidoku.fancafe.grade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 등급(별명/칭호). 관리자가 직접 CRUD하며 코드에 등급명/서열을 하드코딩하지 않는다(20-domain-rules).
 * 변경은 의미 있는 메서드로(setter 미사용).
 */
@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "badge_color", length = 20)
    private String badgeColor;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Grade create(String name, int sortOrder, String badgeColor, boolean isDefault) {
        Grade g = new Grade();
        g.name = name;
        g.sortOrder = sortOrder;
        g.badgeColor = badgeColor;
        g.isDefault = isDefault;
        g.createdAt = LocalDateTime.now();
        return g;
    }

    public void update(String name, int sortOrder, String badgeColor) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.badgeColor = badgeColor;
    }

    public void markDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
