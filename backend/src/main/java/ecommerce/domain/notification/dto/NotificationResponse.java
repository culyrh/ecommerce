package ecommerce.domain.notification.dto;

import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 응답")
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "알림 제목", example = "상품이 재입고되었습니다")
    private String title;

    @Schema(description = "알림 내용", example = "관심 상품 'MacBook Pro'가 재입고되었습니다!")
    private String content;

    @Schema(description = "알림 타입", example = "RESTOCK")
    private NotificationType type;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}