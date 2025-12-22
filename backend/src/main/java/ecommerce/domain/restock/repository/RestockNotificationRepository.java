package ecommerce.domain.restock.repository;

import ecommerce.domain.restock.entity.RestockNotification;
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
}