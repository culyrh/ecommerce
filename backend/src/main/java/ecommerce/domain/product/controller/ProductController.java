package ecommerce.domain.product.controller;

import ecommerce.domain.product.dto.*;
import ecommerce.domain.product.service.ProductService;
import ecommerce.domain.review.dto.ReviewResponse;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록합니다")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content)
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody ProductRequest request
    ) {
        log.info("POST /api/products - email: {}, name: {}", email, request.getName());
        ProductResponse response = productService.createProduct(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 상품 목록 조회", description = "현재 로그인한 판매자의 상품 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "판매자 정보를 찾을 수 없음", content = @Content)
    public ResponseEntity<Page<ProductResponse>> getMyProducts(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/products/my - email: {}", email);
        Page<ProductResponse> response = productService.getMyProducts(email, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 검색/정렬/페이징하여 조회합니다 (인증 불필요)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "DESC") String direction
    ) {
        log.info("GET /api/products - keyword: {}, categoryId: {}, page: {}", keyword, categoryId, page);

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build();

        Page<ProductResponse> response = productService.getProducts(searchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다 (인증 불필요)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "상품 수정", description = "판매자가 본인의 상품을 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 상품 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        log.info("PUT /api/products/{} - email: {}", id, email);
        ProductResponse response = productService.updateProduct(email, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "상품 삭제", description = "판매자가 본인의 상품을 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 상품 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        log.info("DELETE /api/products/{} - email: {}", id, email);
        productService.deleteProduct(email, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/naver/search")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "네이버 쇼핑 API 검색",
            description = "네이버 쇼핑 API를 통해 상품을 검색합니다 (판매자 전용)"
    )
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @ApiResponse(responseCode = "400", description = "검색 키워드 필수", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "422", description = "네이버 API 호출 실패", content = @Content)
    public ResponseEntity<NaverProductSearchResponse> searchNaverProducts(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "10") Integer display,
            @RequestParam(required = false, defaultValue = "1") Integer start
    ) {
        log.info("GET /api/products/naver/search - keyword: {}, display: {}", keyword, display);
        NaverProductSearchResponse response = productService.searchNaverProducts(keyword, start, display);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "재고 업데이트",
            description = "판매자가 본인 상품의 재고를 업데이트합니다 (재입고 이벤트 발행)"
    )
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 상품 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<ProductResponse> updateStock(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        log.info("PUT /api/products/{}/stock - email: {}, stock: {}", id, email, request.getQuantity());
        ProductResponse response = productService.updateStock(email, id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품의 리뷰 목록을 조회합니다 (인증 불필요)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/products/{}/reviews - page: {}", id, pageable.getPageNumber());
        Page<ReviewResponse> response = productService.getProductReviews(id, pageable);
        return ResponseEntity.ok(response);
    }
}