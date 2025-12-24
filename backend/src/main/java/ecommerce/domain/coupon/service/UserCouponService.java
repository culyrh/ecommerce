package ecommerce.domain.coupon.service;

import ecommerce.domain.coupon.dto.UserCouponResponse;
import ecommerce.domain.coupon.entity.Coupon;
import ecommerce.domain.coupon.entity.UserCoupon;
import ecommerce.domain.coupon.repository.CouponRepository;
import ecommerce.domain.coupon.repository.UserCouponRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.common.exception.BadRequestException;
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
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public Page<UserCouponResponse> getMyCoupons(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userCouponRepository.findByUserId(user.getId(), pageable)
                .map(UserCouponResponse::from);
    }

    public Page<UserCouponResponse> getMyAvailableCoupons(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        return userCouponRepository.findAvailableCouponsByUserId(user.getId(), now, pageable)
                .map(UserCouponResponse::from);
    }

    @Transactional
    public UserCouponResponse useCoupon(String email, Long userCouponId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new ResourceNotFoundException("User coupon not found"));

        // 본인 쿠폰인지 확인
        if (!userCoupon.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This coupon does not belong to you");
        }

        // 이미 사용된 쿠폰인지 확인
        if (userCoupon.getIsUsed()) {
            throw new BadRequestException("This coupon has already been used");
        }

        // 쿠폰 만료 확인
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(userCoupon.getExpiresAt())) {
            throw new BadRequestException("This coupon has expired");
        }

        // 쿠폰 사용 처리
        userCoupon.setIsUsed(true);

        return UserCouponResponse.from(userCoupon);
    }

    @Transactional
    public UserCouponResponse issueCouponToUser(User user, Coupon coupon, LocalDateTime expiresAt) {
        // 중복 발급 체크
        boolean exists = userCouponRepository.existsByUserIdAndCouponIdAndIsUsedFalse(
                user.getId(), coupon.getId());
        if (exists) {
            throw new BadRequestException("Coupon already issued to this user");
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .expiresAt(expiresAt)
                .build();

        userCoupon = userCouponRepository.save(userCoupon);
        return UserCouponResponse.from(userCoupon);
    }
}