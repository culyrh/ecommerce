package ecommerce.domain.restock.repository;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.restock.entity.RestockVote;
import ecommerce.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestockVoteRepository extends JpaRepository<RestockVote, Long> {

    Page<RestockVote> findByProductId(Long productId, Pageable pageable);

    Page<RestockVote> findByUserId(Long userId, Pageable pageable);

    Optional<RestockVote> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    // 추가 메서드
    boolean existsByUserAndProduct(User user, Product product);

    Page<RestockVote> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<RestockVote> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);
}