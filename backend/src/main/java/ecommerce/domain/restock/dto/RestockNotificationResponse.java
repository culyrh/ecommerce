package ecommerce.domain.restock.dto;

import ecommerce.domain.product.dto.ProductResponse;
import ecommerce.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "재입고 알림 응답")
public class RestockNotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "상품 정보")
    private ProductResponse product;

    @Schema(description = "사용자 정보")
    private UserResponse user;

    @Schema(description = "알림 발송 여부", example = "false")
    private Boolean isNotified;

    @Schema(description = "알림 신청일시")
    private LocalDateTime createdAt;
}