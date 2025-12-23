package ecommerce.domain.seller.dto;

import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class SellerResponse {

    private Long id;
    private UserResponse user;
    private String businessName;
    private String businessNumber;
    private Integer minStockThreshold;
    private LocalDateTime createdAt;

    public static SellerResponse from(Seller seller) {
        return SellerResponse.builder()
                .id(seller.getId())
                .user(UserResponse.from(seller.getUser()))
                .businessName(seller.getBusinessName())
                .businessNumber(seller.getBusinessNumber())
                .minStockThreshold(seller.getMinStockThreshold())
                .createdAt(seller.getCreatedAt())
                .build();
    }
}