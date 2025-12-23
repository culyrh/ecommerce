package ecommerce.domain.order.controller;

import ecommerce.domain.order.dto.OrderRequest;
import ecommerce.domain.order.dto.OrderResponse;
import ecommerce.domain.order.dto.OrderUpdateRequest;
import ecommerce.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "주문 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다")
    @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "재고 부족", content = @Content)
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody OrderRequest request
    ) {
        log.info("POST /api/orders - email: {}", email);
        OrderResponse response = orderService.createOrder(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "내 주문 목록 조회", description = "내 주문 목록을 페이징하여 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/orders - email: {}, page: {}", email, pageable.getPageNumber());
        Page<OrderResponse> response = orderService.getMyOrders(email, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 주문 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        log.info("GET /api/orders/{} - email: {}", id, email);
        OrderResponse response = orderService.getOrderById(email, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "주문 정보 수정", description = "주문의 배송 정보를 수정합니다 (배송 전만 가능)")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 주문 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "422", description = "주문 상태가 수정 불가능 상태", content = @Content)
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request
    ) {
        log.info("PUT /api/orders/{} - email: {}", id, email);
        OrderResponse response = orderService.updateOrder(email, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다 (배송 전만 가능)")
    @ApiResponse(responseCode = "204", description = "취소 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 주문 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "422", description = "주문 상태가 취소 불가능 상태", content = @Content)
    public ResponseEntity<Void> cancelOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        log.info("DELETE /api/orders/{} - email: {}", id, email);
        orderService.cancelOrder(email, id);
        return ResponseEntity.noContent().build();
    }
}