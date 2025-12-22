package ecommerce.domain.product.entity;

import ecommerce.domain.category.entity.Category;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.seller.entity.Seller;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_seller_id", columnList = "seller_id"),
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_name", columnList = "name"),
        @Index(name = "idx_products_status", columnList = "status"),
        @Index(name = "idx_products_sales_count", columnList = "sales_count")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "naver_product_id", length = 100)
    private String naverProductId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "sales_count")
    @Builder.Default
    private Integer salesCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}