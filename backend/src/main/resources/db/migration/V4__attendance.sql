-- V4: 출석 체크. (회원, 날짜) 1회만(서버 저장). streak/통계는 조회로 계산.

CREATE TABLE attendance (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    member_id   BIGINT   NOT NULL,
    attend_date DATE     NOT NULL,
    created_at  DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_attendance UNIQUE (member_id, attend_date),
    CONSTRAINT fk_attendance_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE INDEX idx_attendance_member ON attendance (member_id, attend_date);
