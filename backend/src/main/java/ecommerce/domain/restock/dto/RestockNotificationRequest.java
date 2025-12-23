package ecommerce.domain.restock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "재입고 알림 신청 요청")
public class RestockNotificationRequest {

    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "1")
    private Long productId;
}