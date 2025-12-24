package ecommerce.domain.coupon.service;

import ecommerce.domain.coupon.dto.CouponRequest;
import ecommerce.domain.coupon.dto.CouponResponse;
import ecommerce.domain.coupon.entity.Coupon;
import ecommerce.domain.coupon.repository.CouponRepository;
import ecommerce.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .name(request.getName())
                .code(generateUniqueCouponCode())
                .type(request.getType())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        coupon = couponRepository.save(coupon);
        return CouponResponse.from(coupon);
    }

    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(CouponResponse::from);
    }

    public Page<CouponResponse> getActiveCoupons(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByValidFromLessThanEqualAndValidUntilGreaterThanEqual(
                now, now, pageable
        ).map(CouponResponse::from);
    }

    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.setName(request.getName());
        coupon.setType(request.getType());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());

        return CouponResponse.from(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        couponRepository.delete(coupon);
    }

    private String generateUniqueCouponCode() {
        String code;
        do {
            code = "COUPON-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        } while (couponRepository.findByCode(code).isPresent());
        return code;
    }
}