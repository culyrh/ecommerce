package ecommerce.domain.coupon.controller;

import ecommerce.domain.coupon.dto.CouponRequest;
import ecommerce.domain.coupon.dto.CouponResponse;
import ecommerce.domain.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "쿠폰 관리 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "쿠폰 생성", description = "관리자가 새로운 쿠폰을 생성합니다")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "쿠폰 목록 조회", description = "쿠폰 목록을 조회합니다")
    public ResponseEntity<Page<CouponResponse>> getCoupons(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CouponResponse> coupons;
        if (activeOnly) {
            coupons = couponService.getActiveCoupons(pageable);
        } else {
            coupons = couponService.getAllCoupons(pageable);
        }
        return ResponseEntity.ok(coupons);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "쿠폰 수정", description = "관리자가 쿠폰 정보를 수정합니다")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "쿠폰 삭제", description = "관리자가 쿠폰을 삭제합니다")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}