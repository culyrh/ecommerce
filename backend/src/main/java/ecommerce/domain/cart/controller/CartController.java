package ecommerce.domain.cart.controller;

import ecommerce.domain.cart.dto.AddToCartRequest;
import ecommerce.domain.cart.dto.CartItemResponse;
import ecommerce.domain.cart.dto.UpdateCartItemRequest;
import ecommerce.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "장바구니 추가", description = "상품을 장바구니에 추가합니다")
    @ApiResponse(responseCode = "201", description = "장바구니 추가 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "422", description = "재고 부족 또는 품절", content = @Content)
    public ResponseEntity<CartItemResponse> addToCart(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody AddToCartRequest request
    ) {
        log.info("POST /api/cart - email: {}, productId: {}", email, request.getProductId());
        CartItemResponse response = cartService.addToCart(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "내 장바구니 조회", description = "내 장바구니 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    public ResponseEntity<List<CartItemResponse>> getMyCart(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("GET /api/cart - email: {}", email);
        List<CartItemResponse> response = cartService.getMyCart(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "장바구니 개수", description = "장바구니에 담긴 상품 개수를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    public ResponseEntity<Long> getCartCount(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("GET /api/cart/count - email: {}", email);
        long count = cartService.getCartCount(email);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "장바구니 수량 변경", description = "장바구니 항목의 수량을 변경합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "장바구니 항목을 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "422", description = "재고 부족", content = @Content)
    public ResponseEntity<CartItemResponse> updateCartItem(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        log.info("PUT /api/cart/{} - email: {}", id, email);
        CartItemResponse response = cartService.updateCartItem(email, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 특정 항목을 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "장바구니 항목을 찾을 수 없음", content = @Content)
    public ResponseEntity<Void> removeCartItem(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        log.info("DELETE /api/cart/{} - email: {}", id, email);
        cartService.removeCartItem(email, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "장바구니 전체 삭제", description = "장바구니를 비웁니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    public ResponseEntity<Void> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("DELETE /api/cart - email: {}", email);
        cartService.clearCart(email);
        return ResponseEntity.noContent().build();
    }
}