package ecommerce.domain.restock.event;

import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.entity.RestockNotification;
import ecommerce.domain.restock.repository.RestockNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 재입고 알림 비동기 발송 서비스
 * RestockEventListener와 분리하여 @Async 프록시 문제 해결
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestockNotificationAsyncService {

    private final RestockNotificationRepository restockNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;

    /**
     * 재입고 알림 발송 (비동기 처리)
     * productId를 받아 새로운 트랜잭션에서 Product 조회 후 알림 발송
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotifications(Long productId) {
        log.info("========================================");
        log.info("재입고 알림 발송 시작: productId={}", productId);
        log.info("========================================");

        try {
            // 새로운 트랜잭션에서 Product 조회
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.error("상품을 찾을 수 없습니다: productId={}", productId);
                return;
            }

            log.info("상품 정보: id={}, name={}, stock={}, status={}",
                    product.getId(), product.getName(), product.getStock(), product.getStatus());

            // 해당 상품의 미발송 알림 신청자 목록 조회
            // productId를 사용하여 조회 (LAZY 로딩 문제 방지)
            List<RestockNotification> notifications = restockNotificationRepository
                    .findByProductIdAndIsNotifiedFalse(productId);

            log.info("조회된 알림 신청자 수: {}", notifications.size());

            if (notifications.isEmpty()) {
                log.info("⚠️ 재입고 알림 신청자 없음: productId={}", productId);
                log.info("========================================");
                return;
            }

            log.info("알림 발송 시작: productId={}, 상품명={}, 신청자수={}",
                    productId, product.getName(), notifications.size());

            int successCount = 0;
            int failCount = 0;

            // 각 사용자에게 알림 생성
            for (RestockNotification restockNotification : notifications) {
                try {
                    String message = String.format("'%s' 상품이 재입고되었습니다!", product.getName());

                    log.debug("알림 생성 중: userId={}, userName={}",
                            restockNotification.getUser().getId(),
                            restockNotification.getUser().getName());

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

                    successCount++;
                    log.info("알림 발송 성공 [{}/{}]: userId={}, userName={}, email={}",
                            successCount, notifications.size(),
                            restockNotification.getUser().getId(),
                            restockNotification.getUser().getName(),
                            restockNotification.getUser().getEmail());

                } catch (Exception e) {
                    failCount++;
                    log.error("알림 발송 실패 [{}/{}]: userId={}, userName={}, 에러={}",
                            failCount, notifications.size(),
                            restockNotification.getUser().getId(),
                            restockNotification.getUser().getName(),
                            e.getMessage(), e);
                }
            }

            log.info("========================================");
            log.info("재입고 알림 발송 완료 통계");
            log.info("  - 상품: {} (ID: {})", product.getName(), productId);
            log.info("  - 성공: {}건", successCount);
            log.info("  - 실패: {}건", failCount);
            log.info("  - 총 처리: {}건", notifications.size());
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("재입고 알림 발송 중 예외 발생");
            log.error("  - productId: {}", productId);
            log.error("  - 에러 메시지: {}", e.getMessage());
            log.error("  - 스택 트레이스:", e);
            log.error("========================================");
        }
    }
}