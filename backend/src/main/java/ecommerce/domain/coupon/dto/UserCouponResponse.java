package ecommerce.domain.coupon.dto;

import ecommerce.domain.coupon.entity.UserCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 응답")
public class UserCouponResponse {

    @Schema(description = "사용자 쿠폰 ID", example = "1")
    private Long id;

    @Schema(description = "쿠폰 정보")
    private CouponResponse coupon;

    @Schema(description = "사용 여부", example = "false")
    private Boolean isUsed;

    @Schema(description = "발급 일시")
    private LocalDateTime issuedAt;

    @Schema(description = "만료 일시")
    private LocalDateTime expiresAt;

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .coupon(CouponResponse.from(userCoupon.getCoupon()))
                .isUsed(userCoupon.getIsUsed())
                .issuedAt(userCoupon.getIssuedAt())
                .expiresAt(userCoupon.getExpiresAt())
                .build();
    }
}