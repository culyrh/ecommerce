package ecommerce.domain.restock.event;

import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.entity.RestockNotification;
import ecommerce.domain.restock.repository.RestockNotificationRepository;
import ecommerce.domain.restock.repository.RestockVoteRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestockEventListener {

    private final RestockNotificationRepository restockNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final RestockVoteRepository restockVoteRepository;
    private final RedisService redisService;

    private static final String VOTE_COUNT_KEY_PREFIX = "restock:vote:count:";

    /**
     * 재입고 투표 초기화 (동기)
     * 트랜잭션 커밋 후 즉시 실행하여 데이터 정합성 보장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleRestockVoteCleanup(ProductRestockedEvent event) {
        log.info("재입고 투표 초기화 시작: 상품ID={}", event.getProductId());

        // 재고가 0 → 1+ 변경된 경우만 처리
        if (event.getPreviousStock() > 0 || event.getCurrentStock() <= 0) {
            log.info("재입고 조건 미충족 - 투표 초기화 생략");
            return;
        }

        // 상품 조회
        Product product = productRepository.findById(event.getProductId())
                .orElse(null);

        if (product == null) {
            log.error("상품을 찾을 수 없습니다: ID={}", event.getProductId());
            return;
        }

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

    /**
     * 재입고 알림 발송 (비동기)
     * 알림 발송이 실패해도 투표 초기화에는 영향 없음
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleRestockNotification(ProductRestockedEvent event) {
        log.info("재입고 알림 발송 시작: 상품ID={}", event.getProductId());

        // 재고가 0 → 1+ 변경된 경우만 처리
        if (event.getPreviousStock() > 0 || event.getCurrentStock() <= 0) {
            log.info("재입고 조건 미충족 - 알림 발송 생략");
            return;
        }

        // 상품 조회
        Product product = productRepository.findById(event.getProductId())
                .orElse(null);

        if (product == null) {
            log.error("상품을 찾을 수 없습니다: ID={}", event.getProductId());
            return;
        }

        // 해당 상품의 미발송 알림 신청자 목록 조회
        List<RestockNotification> notifications = restockNotificationRepository
                .findByProductAndIsNotifiedFalse(product);

        if (notifications.isEmpty()) {
            log.info("재입고 알림 신청자 없음: 상품={}", product.getName());
            return;
        }

        log.info("재입고 알림 발송 시작: 상품={}, 신청자수={}", product.getName(), notifications.size());

        // 각 사용자에게 알림 생성
        for (RestockNotification restockNotification : notifications) {
            try {
                String message = String.format("'%s' 상품이 재입고되었습니다!", product.getName());

                Notification notification = Notification.builder()
                        .user(restockNotification.getUser())
                        .type(NotificationType.RESTOCK)
                        .title("재입고 알림")
                        .content(message)
                        .isRead(false)
                        .build();

                notificationRepository.save(notification);

                // 알림 발송 플래그 업데이트 (삭제하지 않음)
                restockNotification.setIsNotified(true);
                restockNotificationRepository.save(restockNotification);

                log.info("재입고 알림 발송 완료: userId={}, product={}",
                        restockNotification.getUser().getId(), product.getName());
            } catch (Exception e) {
                log.error("재입고 알림 발송 실패: 사용자ID={}, 에러={}",
                        restockNotification.getUser().getId(), e.getMessage());
            }
        }

        log.info("재입고 알림 처리 완료: 상품={}, 발송수={}", product.getName(), notifications.size());
    }
}