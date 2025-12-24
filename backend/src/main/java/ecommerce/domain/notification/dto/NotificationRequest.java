package ecommerce.domain.notification.dto;

import ecommerce.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 생성 요청")
public class NotificationRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @NotBlank(message = "제목은 필수입니다")
    @Schema(description = "알림 제목", example = "상품이 재입고되었습니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Schema(description = "알림 내용", example = "관심 상품 'MacBook Pro'가 재입고되었습니다!")
    private String content;

    @NotNull(message = "알림 타입은 필수입니다")
    @Schema(description = "알림 타입", example = "RESTOCK")
    private NotificationType type;
}