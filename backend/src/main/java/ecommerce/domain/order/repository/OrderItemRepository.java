package ecommerce.domain.order.repository;

import ecommerce.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.product.id = :productId " +
            "AND oi.createdAt >= :fromDate")
    Long sumQuantityByProductIdAndCreatedAtAfter(
            @Param("productId") Long productId,
            @Param("fromDate") LocalDateTime fromDate
    );
}