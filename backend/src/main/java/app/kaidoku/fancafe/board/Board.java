package app.kaidoku.fancafe.board;

import app.kaidoku.fancafe.common.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** 게시판. 동적(관리자 CRUD)이며 코드에 게시판명을 하드코딩하지 않는다. */
@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(length = 500)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardType type;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Enumerated(EnumType.STRING)
    @Column(name = "write_role", nullable = false, length = 20)
    private Role writeRole;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 말머리(카테고리) 라벨을 쉼표로 이어붙인 원문. 예: "공지,자랑,질문". 비면 말머리 미사용. */
    @Column(length = 500)
    private String categories;

    /** 게시판 생성(관리자). 기본 노출 상태로 만든다. */
    public static Board create(String code, String nameKr, String nameEn, String description,
                               int sortOrder, BoardType type, Role writeRole, String categories) {
        Board b = new Board();
        b.code = code;
        b.nameKr = nameKr;
        b.nameEn = nameEn;
        b.description = description;
        b.sortOrder = sortOrder;
        b.type = type;
        b.visible = true;
        b.writeRole = writeRole;
        b.categories = categories;
        b.createdAt = LocalDateTime.now();
        return b;
    }

    public void changeVisibility(boolean visible) {
        this.visible = visible;
    }

    /** 게시판 설정 수정(관리자). code(URL 슬러그)는 링크 안정성을 위해 변경하지 않는다. */
    public void update(String nameKr, String nameEn, String description,
                       int sortOrder, BoardType type, Role writeRole, boolean visible, String categories) {
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.description = description;
        this.sortOrder = sortOrder;
        this.type = type;
        this.writeRole = writeRole;
        this.visible = visible;
        this.categories = categories;
    }

    /** 말머리 라벨 목록(쉼표 분리, 트림, 빈값 제거). null/빈 문자열이면 빈 목록. */
    public List<String> categoryList() {
        if (categories == null || categories.isBlank()) {
            return List.of();
        }
        return Arrays.stream(categories.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
