package ecommerce.domain.scheduler;

import ecommerce.domain.order.entity.OrderItem;
import ecommerce.domain.order.repository.OrderItemRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 판매 순위 캐시 워밍 스케줄러
 * 매시간 정각에 실행되어 판매자별 상품 판매 순위를 Redis에 캐싱
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesRankingCacheScheduler {

    private final SellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedisService redisService;

    private static final String REDIS_RANKING_PREFIX = "seller:ranking:";
    private static final Long CACHE_TTL = 3600L; // 1시간

    @Scheduled(cron = "0 0 * * * ?") // 매시간 정각 실행
    @Transactional(readOnly = true)
    public void updateSalesRanking() {
        log.info("========================================");
        log.info("판매 순위 캐시 워밍 시작");
        log.info("========================================");

        List<Seller> sellers = sellerRepository.findAll();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        int successCount = 0;
        int failCount = 0;

        for (Seller seller : sellers) {
            try {
                // 판매자의 최근 7일 주문 아이템 조회
                List<OrderItem> orderItems = orderItemRepository.findAll().stream()
                        .filter(item -> item.getSeller().getId().equals(seller.getId()))
                        .filter(item -> item.getCreatedAt().isAfter(sevenDaysAgo))
                        .collect(Collectors.toList());

                if (orderItems.isEmpty()) {
                    log.debug("판매자 {}의 최근 7일 판매 데이터 없음", seller.getId());
                    continue;
                }

                // 상품별로 그룹화
                Map<Product, List<OrderItem>> groupedByProduct = orderItems.stream()
                        .collect(Collectors.groupingBy(OrderItem::getProduct));

                // 순위 데이터 생성 (형식: "productId:salesCount:revenue|...")
                String rankingData = groupedByProduct.entrySet().stream()
                        .map(entry -> {
                            Product product = entry.getKey();
                            List<OrderItem> items = entry.getValue();

                            long salesCount = items.stream()
                                    .mapToLong(OrderItem::getQuantity)
                                    .sum();

                            BigDecimal revenue = items.stream()
                                    .map(OrderItem::getSubtotal)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            return product.getId() + ":" + salesCount + ":" + revenue;
                        })
                        .collect(Collectors.joining("|"));

                // Redis에 저장
                String key = REDIS_RANKING_PREFIX + seller.getId();
                redisService.setValue(key, rankingData, CACHE_TTL);

                successCount++;
                log.debug("판매자 {} 순위 캐시 업데이트 완료", seller.getId());

            } catch (Exception e) {
                failCount++;
                log.error("판매자 {} 순위 캐시 업데이트 실패: {}", seller.getId(), e.getMessage(), e);
            }
        }

        log.info("========================================");
        log.info("판매 순위 캐시 워밍 완료");
        log.info("성공: {}개, 실패: {}개", successCount, failCount);
        log.info("========================================");
    }
}