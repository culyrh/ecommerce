package ecommerce.domain.coupon.repository;

import ecommerce.domain.coupon.entity.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    Page<UserCoupon> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndCouponIdAndIsUsedFalse(Long userId, Long couponId);
}