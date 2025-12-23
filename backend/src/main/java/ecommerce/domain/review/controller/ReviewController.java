package ecommerce.domain.review.controller;

import ecommerce.domain.review.dto.ReviewRequest;
import ecommerce.domain.review.dto.ReviewResponse;
import ecommerce.domain.review.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "리뷰 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "리뷰 작성", description = "상품에 대한 리뷰를 작성합니다")
    @ApiResponse(responseCode = "201", description = "리뷰 작성 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody ReviewRequest request
    ) {
        log.info("POST /api/reviews - email: {}, productId: {}", email, request.getProductId());
        ReviewResponse response = reviewService.createReview(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "내 리뷰 목록 조회", description = "내가 작성한 리뷰 목록을 페이징하여 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/reviews/my - email: {}, page: {}", email, pageable.getPageNumber());
        Page<ReviewResponse> response = reviewService.getMyReviews(email, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 리뷰 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content)
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request
    ) {
        log.info("PUT /api/reviews/{} - email: {}", id, email);
        ReviewResponse response = reviewService.updateReview(email, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 리뷰 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content)
    public ResponseEntity<Void> deleteReview(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        log.info("DELETE /api/reviews/{} - email: {}", id, email);
        reviewService.deleteReview(email, id);
        return ResponseEntity.noContent().build();
    }
}