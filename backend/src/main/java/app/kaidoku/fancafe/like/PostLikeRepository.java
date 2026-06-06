package app.kaidoku.fancafe.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByMember_IdAndPost_Id(Long memberId, Long postId);

    boolean existsByMember_IdAndPost_Id(Long memberId, Long postId);
}
