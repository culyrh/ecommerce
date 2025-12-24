package ecommerce.domain.coupon.repository;

import ecommerce.domain.coupon.entity.Coupon;
import ecommerce.domain.coupon.enums.CouponType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findFirstByType(CouponType type);

    // 현재 유효한 쿠폰 조회 (유효기간 내)
    Page<Coupon> findByValidFromLessThanEqualAndValidUntilGreaterThanEqual(
            LocalDateTime validFrom, LocalDateTime validUntil, Pageable pageable);

    // 쿠폰 타입별 조회
    List<Coupon> findByType(CouponType type);
}