package ecommerce.domain.seller.entity;

import ecommerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_sellers_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(name = "min_stock_threshold")
    private Integer minStockThreshold = 10;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}