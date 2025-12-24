package ecommerce.domain.notification.service;

import ecommerce.domain.notification.dto.NotificationRequest;
import ecommerce.domain.notification.dto.NotificationResponse;
import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }

    public Page<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(NotificationResponse::from);
    }

    public Page<NotificationResponse> getMyUnreadNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findByUserIdAndIsReadFalse(user.getId(), pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // 본인 알림인지 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This notification does not belong to you");
        }

        notification.setIsRead(true);

        return NotificationResponse.from(notification);
    }

    @Transactional
    public void deleteNotification(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // 본인 알림인지 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This notification does not belong to you");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationRepository.findUnreadByUserId(user.getId())
                .forEach(notification -> notification.setIsRead(true));
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}