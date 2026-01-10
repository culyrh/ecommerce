package ecommerce.domain.restock.event;

import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.product.entity.Product;
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

    /**
     * 재입고 알림 발송 (비동기 처리)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotifications(Product product) {
        log.info("재입고 알림 발송 시작: 상품={}", product.getName());

        // 해당 상품의 미발송 알림 신청자 목록 조회
        List<RestockNotification> notifications = restockNotificationRepository
                .findByProductAndIsNotifiedFalse(product);

        if (notifications.isEmpty()) {
            log.info("재입고 알림 신청자 없음: 상품={}", product.getName());
            return;
        }

        log.info("재입고 알림 발송 시작: 상품={}, 신청자수={}",
                product.getName(), notifications.size());

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

        log.info("재입고 알림 처리 완료: 상품={}, 발송수={}",
                product.getName(), notifications.size());
    }
}