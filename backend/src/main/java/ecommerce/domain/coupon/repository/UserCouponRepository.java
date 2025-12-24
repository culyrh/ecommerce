package ecommerce.domain.coupon.repository;

import ecommerce.domain.coupon.entity.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    Page<UserCoupon> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndCouponIdAndIsUsedFalse(Long userId, Long couponId);

    // 사용 가능한 쿠폰 조회 (미사용 + 만료 전)
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId " +
            "AND uc.isUsed = false AND uc.expiresAt > :now")
    Page<UserCoupon> findAvailableCouponsByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}