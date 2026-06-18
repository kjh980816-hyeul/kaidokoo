-- V8: 게시글 첨부 이미지(파일 업로드). post 1 : N post_image, 정렬 순서 보존.
-- 파일은 FileStorageService가 /uploads/posts/ 아래에 저장하고 URL만 여기 기록한다.
CREATE TABLE post_image (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post(id)
);

CREATE INDEX idx_post_image_post ON post_image (post_id, sort_order);
