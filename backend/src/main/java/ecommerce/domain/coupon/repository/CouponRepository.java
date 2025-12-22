package ecommerce.domain.coupon.repository;

import ecommerce.domain.coupon.entity.Coupon;
import ecommerce.domain.coupon.enums.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findFirstByType(CouponType type);
}