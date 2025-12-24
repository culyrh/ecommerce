package ecommerce.domain.coupon.dto;

import ecommerce.domain.coupon.enums.CouponType;
import ecommerce.domain.coupon.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 생성/수정 요청")
public class CouponRequest {

    @NotBlank(message = "쿠폰 이름은 필수입니다")
    @Schema(description = "쿠폰 이름", example = "신규 가입 환영 쿠폰")
    private String name;

    @NotNull(message = "쿠폰 타입은 필수입니다")
    @Schema(description = "쿠폰 타입 (WELCOME/BIRTHDAY/VIP/GENERAL)", example = "WELCOME")
    private CouponType type;

    @NotNull(message = "할인 타입은 필수입니다")
    @Schema(description = "할인 타입 (PERCENTAGE/FIXED_AMOUNT)", example = "PERCENTAGE")
    private DiscountType discountType;

    @NotNull(message = "할인 값은 필수입니다")
    @Positive(message = "할인 값은 양수여야 합니다")
    @Schema(description = "할인 값 (퍼센트면 10, 고정금액이면 5000)", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "최소 주문 금액", example = "30000")
    private BigDecimal minOrderAmount;

    @NotNull(message = "유효 시작일은 필수입니다")
    @Schema(description = "유효 시작일", example = "2025-01-01T00:00:00")
    private LocalDateTime validFrom;

    @NotNull(message = "유효 종료일은 필수입니다")
    @Schema(description = "유효 종료일", example = "2025-12-31T23:59:59")
    private LocalDateTime validUntil;
}