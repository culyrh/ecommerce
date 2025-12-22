package ecommerce.domain.order.entity;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.seller.entity.Seller;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_product_id", columnList = "product_id"),
        @Index(name = "idx_order_items_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}