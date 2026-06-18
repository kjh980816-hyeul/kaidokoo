package app.kaidoku.fancafe.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    /**
     * 여러 글의 이미지를 (글id, 정렬순) 순서로 한 번에 조회 — 목록 썸네일용.
     * {@code [postId, url]} 행을 돌려주며, 같은 글에서 먼저 나온 행이 대표 썸네일이 된다.
     * {@code pi.post.id}는 FK 컬럼만 읽어 Post를 추가 로딩하지 않는다(N+1 회피).
     */
    @Query("""
            select pi.post.id, pi.url from PostImage pi
            where pi.post.id in :ids
            order by pi.post.id asc, pi.sortOrder asc
            """)
    List<Object[]> findThumbnailRows(@Param("ids") Collection<Long> ids);
}
