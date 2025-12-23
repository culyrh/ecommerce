package ecommerce.domain.product.dto;

import ecommerce.domain.category.dto.CategoryResponse;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.seller.dto.SellerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private SellerResponse seller;
    private CategoryResponse category;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String naverProductId;
    private ProductStatus status;
    private Integer salesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .seller(product.getSeller() != null ? SellerResponse.from(product.getSeller()) : null)
                .category(product.getCategory() != null ? CategoryResponse.fromWithoutChildren(product.getCategory()) : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .naverProductId(product.getNaverProductId())
                .status(product.getStatus())
                .salesCount(product.getSalesCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static ProductResponse fromSimple(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .salesCount(product.getSalesCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}