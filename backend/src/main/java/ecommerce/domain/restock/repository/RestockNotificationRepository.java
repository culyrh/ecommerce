package ecommerce.domain.restock.repository;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.restock.entity.RestockNotification;
import ecommerce.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestockNotificationRepository extends JpaRepository<RestockNotification, Long> {

    Page<RestockNotification> findByProductId(Long productId, Pageable pageable);

    Page<RestockNotification> findByUserId(Long userId, Pageable pageable);

    Optional<RestockNotification> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    List<RestockNotification> findByProductIdAndIsNotifiedFalse(Long productId);

    // 추가 메서드
    boolean existsByUserAndProduct(User user, Product product);

    Page<RestockNotification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<RestockNotification> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);

    List<RestockNotification> findByProductAndIsNotifiedFalse(Product product);

    // 추가: isNotified=true인 알림 조회 (재입고 시 플래그 초기화용)
    List<RestockNotification> findByProductIdAndIsNotifiedTrue(Long productId);
}