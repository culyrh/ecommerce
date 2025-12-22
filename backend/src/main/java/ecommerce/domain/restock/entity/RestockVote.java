package ecommerce.domain.restock.entity;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restock_votes",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_restock_vote", columnNames = {"product_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_restock_votes_product_id", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}