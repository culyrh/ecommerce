package ecommerce.domain.scheduler;

import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.order.entity.OrderItem;
import ecommerce.domain.order.repository.OrderItemRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 자동 발주 권장 스케줄러
 * 매일 새벽 1시에 실행되어 재고 소진 예상 상품에 대해 발주 권장 알림 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockReorderScheduler {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationRepository notificationRepository;
    private final RedisService redisService;

    private static final String REDIS_REORDER_ALERT_PREFIX = "reorder:alert:";
    private static final int CRITICAL_DAYS = 3; // 소진 예상 일수 임계값

    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시 실행
    @Transactional
    public void checkStockLevels() {
        log.info("========================================");
        log.info("자동 발주 권장 체크 시작");
        log.info("========================================");

        // 활성 상품 중 재고가 있는 상품만 조회
        List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE && p.getStock() > 0)
                .collect(Collectors.toList());

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int alertCount = 0;
        int skippedCount = 0;

        log.info("체크 대상 활성 상품 수: {}", activeProducts.size());

        for (Product product : activeProducts) {
            try {
                // 최근 7일간 판매량 집계
                List<OrderItem> orderItems = orderItemRepository.findAll().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()))
                        .filter(item -> item.getCreatedAt().isAfter(sevenDaysAgo))
                        .collect(Collectors.toList());

                if (orderItems.isEmpty()) {
                    skippedCount++;
                    continue;
                }

                // 총 판매량 계산
                long totalSales = orderItems.stream()
                        .mapToLong(OrderItem::getQuantity)
                        .sum();

                // 일평균 판매량 계산
                double dailyAverage = totalSales / 7.0;

                if (dailyAverage <= 0) {
                    skippedCount++;
                    continue;
                }

                // 예상 소진 일수 계산
                double daysUntilStockout = product.getStock() / dailyAverage;

                // 소진 예상 일수가 임계값 이하이면 알림 생성
                if (daysUntilStockout <= CRITICAL_DAYS) {
                    String redisKey = REDIS_REORDER_ALERT_PREFIX + product.getId();

                    // 중복 알림 방지 (24시간 내 이미 알림 생성된 경우 스킵)
                    if (redisService.hasKey(redisKey)) {
                        log.debug("상품 {} 중복 알림 스킵 (24시간 내 이미 발송)", product.getId());
                        skippedCount++;
                        continue;
                    }

                    // 판매자 알림 생성
                    User sellerUser = product.getSeller().getUser();

                    Notification notification = Notification.builder()
                            .user(sellerUser)
                            .type(NotificationType.STOCK_ALERT)
                            .title("재고 부족 알림")
                            .content(String.format(
                                    "상품 '%s'의 재고가 부족합니다. " +
                                            "현재 재고: %d개, 일평균 판매량: %.1f개, 예상 소진: %.1f일 후",
                                    product.getName(),
                                    product.getStock(),
                                    dailyAverage,
                                    daysUntilStockout
                            ))
                            .isRead(false)
                            .build();

                    notificationRepository.save(notification);

                    // Redis에 알림 플래그 저장 (24시간 TTL)
                    redisService.setValue(redisKey, "1", 86400L);

                    alertCount++;
                    log.info("✅ 재고 부족 알림 생성: 상품 '{}', 현재 재고 {}개, 예상 소진 {:.1f}일",
                            product.getName(), product.getStock(), daysUntilStockout);
                }

            } catch (Exception e) {
                log.error("상품 {} 재고 체크 실패: {}", product.getId(), e.getMessage(), e);
            }
        }

        log.info("========================================");
        log.info("자동 발주 권장 체크 완료");
        log.info("알림 생성: {}건, 스킵: {}건", alertCount, skippedCount);
        log.info("========================================");
    }
}