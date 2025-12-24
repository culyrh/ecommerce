package ecommerce.domain.coupon.dto;

import ecommerce.domain.coupon.entity.Coupon;
import ecommerce.domain.coupon.enums.CouponType;
import ecommerce.domain.coupon.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "쿠폰 응답")
public class CouponResponse {

    @Schema(description = "쿠폰 ID", example = "1")
    private Long id;

    @Schema(description = "쿠폰 이름", example = "신규 가입 환영 쿠폰")
    private String name;

    @Schema(description = "쿠폰 코드", example = "COUPON-1234567890-5678")
    private String code;

    @Schema(description = "쿠폰 타입", example = "WELCOME")
    private CouponType type;

    @Schema(description = "할인 타입", example = "PERCENTAGE")
    private DiscountType discountType;

    @Schema(description = "할인 값", example = "10")
    private BigDecimal discountValue;

    @Schema(description = "최소 주문 금액", example = "30000")
    private BigDecimal minOrderAmount;

    @Schema(description = "유효 시작일")
    private LocalDateTime validFrom;

    @Schema(description = "유효 종료일")
    private LocalDateTime validUntil;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .code(coupon.getCode())
                .type(coupon.getType())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}