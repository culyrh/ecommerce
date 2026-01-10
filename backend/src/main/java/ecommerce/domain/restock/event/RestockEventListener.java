package ecommerce.domain.restock.event;

import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.repository.RestockVoteRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    private final RedisService redisService;
    private final RestockNotificationAsyncService notificationAsyncService;

    private static final String VOTE_COUNT_KEY_PREFIX = "restock:vote:count:";

    /**
     * 재입고 이벤트 처리
     * 1. 투표 초기화 (동기)
     * 2. 알림 발송 (비동기 호출)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductRestocked(ProductRestockedEvent event) {
        log.info("재입고 이벤트 수신: 상품ID={}, 이전재고={}, 현재재고={}",
                event.getProductId(), event.getPreviousStock(), event.getCurrentStock());

        // 재고가 0 → 1+ 변경된 경우만 처리
        if (event.getPreviousStock() > 0 || event.getCurrentStock() <= 0) {
            log.info("재입고 조건 미충족 - 이벤트 무시");
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

        // 2. 알림 발송 (비동기 서비스 호출 - eventually consistent)
        notificationAsyncService.sendNotifications(product);
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
            log.info("재입고 투표 초기화 완료: 상품={}, 삭제된 투표수={}",
                    product.getName(), votes.size());
        }

        // Redis 투표 카운트 초기화
        String voteCountKey = VOTE_COUNT_KEY_PREFIX + product.getId();
        redisService.delete(voteCountKey);
        log.info("Redis 투표 카운트 초기화 완료: key={}", voteCountKey);
    }
}