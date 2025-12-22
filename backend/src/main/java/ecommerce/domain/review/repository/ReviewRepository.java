package ecommerce.domain.review.repository;

import ecommerce.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);
}