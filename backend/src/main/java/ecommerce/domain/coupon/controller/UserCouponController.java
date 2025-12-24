package ecommerce.domain.coupon.controller;

import ecommerce.domain.coupon.dto.UserCouponResponse;
import ecommerce.domain.coupon.service.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-coupons")
@RequiredArgsConstructor
@Tag(name = "User Coupon", description = "사용자 쿠폰 API")
public class UserCouponController {

    private final UserCouponService userCouponService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "내 쿠폰 목록 조회", description = "내가 보유한 쿠폰 목록을 조회합니다")
    public ResponseEntity<Page<UserCouponResponse>> getMyCoupons(
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly,
            @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<UserCouponResponse> coupons;

        if (availableOnly) {
            coupons = userCouponService.getMyAvailableCoupons(email, pageable);
        } else {
            coupons = userCouponService.getMyCoupons(email, pageable);
        }

        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/{id}/use")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "쿠폰 사용", description = "보유한 쿠폰을 사용 처리합니다")
    public ResponseEntity<UserCouponResponse> useCoupon(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        UserCouponResponse response = userCouponService.useCoupon(email, id);
        return ResponseEntity.ok(response);
    }
}