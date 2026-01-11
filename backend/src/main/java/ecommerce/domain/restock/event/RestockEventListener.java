package ecommerce.domain.restock.event;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.entity.RestockNotification;
import ecommerce.domain.restock.repository.RestockNotificationRepository;
import ecommerce.domain.restock.repository.RestockVoteRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 재입고 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestockEventListener {

    private final ProductRepository productRepository;
    private final RestockVoteRepository restockVoteRepository;
    private final RestockNotificationRepository restockNotificationRepository;
    private final RedisService redisService;
    private final RestockNotificationAsyncService notificationAsyncService;

    private static final String VOTE_COUNT_KEY_PREFIX = "restock:vote:count:";

    /**
     * 재입고 이벤트 처리
     * 1. 투표 초기화 (동기)
     * 2. 알림 플래그 초기화 (동기)
     * 3. 알림 발송 (비동기 호출) - productId 전달
     */
    @EventListener
    @Transactional
    public void handleProductRestocked(ProductRestockedEvent event) {
        log.info("=== 재입고 이벤트 수신: 상품ID={}, 이전재고={}, 현재재고={} ===",
                event.getProductId(), event.getPreviousStock(), event.getCurrentStock());

        // 재고가 0 → 1+ 변경된 경우만 처리
        if (event.getPreviousStock() > 0 || event.getCurrentStock() <= 0) {
            log.info("⚠️ 재입고 조건 미충족 - 이벤트 무시");
            return;
        }

        // 상품 조회
        Product product = productRepository.findById(event.getProductId())
                .orElse(null);

        if (product == null) {
            log.error("상품을 찾을 수 없습니다: ID={}", event.getProductId());
            return;
        }

        // 1. 투표 초기화 (동기 처리 - 강한 정합성)
        cleanupRestockVotes(product);

        // 2. 알림 플래그 초기화 (동기 처리) 추가
        resetNotificationFlags(product);

        // 3. 알림 발송 (비동기 서비스 호출 - productId만 전달)
        // Product 엔티티 대신 productId를 전달하여 LAZY 로딩 문제 해결
        log.info("비동기 알림 발송 시작: productId={}", event.getProductId());
        notificationAsyncService.sendNotifications(event.getProductId());

        log.info("=== 재입고 이벤트 처리 완료: 상품ID={} ===", event.getProductId());
    }

    /**
     * 재입고 투표 초기화 (동기 처리)
     */
    private void cleanupRestockVotes(Product product) {
        log.info("재입고 투표 초기화 시작: 상품={}", product.getName());

        // 재입고 투표 초기화 (DB에서 삭제)
        List<ecommerce.domain.restock.entity.RestockVote> votes =
                restockVoteRepository.findByProduct(product);

        if (!votes.isEmpty()) {
            restockVoteRepository.deleteAll(votes);
            restockVoteRepository.flush(); // 즉시 DB에 반영
            log.info("재입고 투표 DB 삭제 완료: 상품={}, 삭제된 투표수={}",
                    product.getName(), votes.size());
        } else {
            log.info("삭제할 투표 없음: 상품={}", product.getName());
        }

        // Redis 투표 카운트 초기화
        String voteCountKey = VOTE_COUNT_KEY_PREFIX + product.getId();
        redisService.delete(voteCountKey);
        log.info("Redis 투표 카운트 초기화 완료: key={}", voteCountKey);
    }

    /**
     * 재입고 알림 플래그 초기화 (동기 처리) 추가
     * 이전 재입고에서 발송된 알림(isNotified=true)을 다시 사용 가능하도록 초기화
     */
    private void resetNotificationFlags(Product product) {
        log.info("재입고 알림 플래그 초기화 시작: 상품={}", product.getName());

        // isNotified=true인 알림 조회
        List<RestockNotification> notifiedAlerts =
                restockNotificationRepository.findByProductIdAndIsNotifiedTrue(product.getId());

        if (!notifiedAlerts.isEmpty()) {
            // isNotified를 false로 변경하여 다시 알림 받을 수 있도록 설정
            for (RestockNotification notification : notifiedAlerts) {
                notification.setIsNotified(false);
                log.debug("알림 플래그 초기화: userId={}, 이전 isNotified=true → false",
                        notification.getUser().getId());
            }

            restockNotificationRepository.saveAll(notifiedAlerts);
            restockNotificationRepository.flush(); // 즉시 DB에 반영

            log.info("재입고 알림 플래그 초기화 완료: 상품={}, 초기화된 알림수={}",
                    product.getName(), notifiedAlerts.size());
        } else {
            log.info("ℹ초기화할 알림 플래그 없음: 상품={}", product.getName());
        }
    }
}