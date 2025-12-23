package ecommerce.domain.restock.controller;

import ecommerce.domain.restock.dto.RestockNotificationRequest;
import ecommerce.domain.restock.dto.RestockNotificationResponse;
import ecommerce.domain.restock.service.RestockNotificationService;
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

@Tag(name = "Restock Notifications", description = "재입고 알림 API")
@RestController
@RequestMapping("/api/restock-notifications")
@RequiredArgsConstructor
public class RestockNotificationController {

    private final RestockNotificationService restockNotificationService;

    @Operation(summary = "재입고 알림 신청", description = "상품 재입고 시 알림을 받도록 신청합니다")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RestockNotificationResponse> subscribeRestock(
            @Valid @RequestBody RestockNotificationRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        RestockNotificationResponse response = restockNotificationService.subscribeRestock(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 알림 신청 목록 조회", description = "본인이 신청한 재입고 알림 목록을 조회합니다")
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<RestockNotificationResponse>> getMySubscriptions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        String email = authentication.getName();
        Page<RestockNotificationResponse> notifications = restockNotificationService.getMySubscriptions(email, pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "상품별 알림 신청 목록 조회", description = "특정 상품에 대한 재입고 알림 신청 목록을 조회합니다 (판매자만 가능)")
    @GetMapping("/products/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Page<RestockNotificationResponse>> getProductSubscriptions(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        String email = authentication.getName();
        Page<RestockNotificationResponse> notifications = restockNotificationService.getProductSubscriptions(email, productId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 신청 취소", description = "재입고 알림 신청을 취소합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unsubscribeRestock(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        restockNotificationService.unsubscribeRestock(email, id);
        return ResponseEntity.noContent().build();
    }
}