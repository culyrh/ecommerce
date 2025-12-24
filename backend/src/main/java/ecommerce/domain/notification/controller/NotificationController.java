package ecommerce.domain.notification.controller;

import ecommerce.domain.notification.dto.NotificationRequest;
import ecommerce.domain.notification.dto.NotificationResponse;
import ecommerce.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "알림 생성", description = "관리자가 사용자에게 알림을 생성합니다")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "내 알림 목록 조회", description = "내 알림 목록을 조회합니다")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        String email = authentication.getName();
        Page<NotificationResponse> notifications;

        if (unreadOnly) {
            notifications = notificationService.getMyUnreadNotifications(email, pageable);
        } else {
            notifications = notificationService.getMyNotifications(email, pageable);
        }

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "읽지 않은 알림 개수", description = "읽지 않은 알림 개수를 조회합니다")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        long count = notificationService.getUnreadCount(email);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        NotificationResponse response = notificationService.markAsRead(email, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "모든 알림 읽음 처리", description = "내 모든 알림을 읽음 상태로 변경합니다")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        notificationService.markAllAsRead(email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "알림 삭제", description = "알림을 삭제합니다")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        notificationService.deleteNotification(email, id);
        return ResponseEntity.noContent().build();
    }
}