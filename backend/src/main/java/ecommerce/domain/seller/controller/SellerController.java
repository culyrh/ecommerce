package ecommerce.domain.seller.controller;

import ecommerce.domain.seller.dto.DashboardResponse;
import ecommerce.domain.seller.dto.SellerRequest;
import ecommerce.domain.seller.dto.SellerResponse;
import ecommerce.domain.seller.service.DashboardService;
import ecommerce.domain.seller.service.SellerService;
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

@Slf4j
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Tag(name = "Sellers", description = "판매자 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class SellerController {

    private final SellerService sellerService;
    private final DashboardService dashboardService; // 의존성 주입 추가 필요

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "판매자 등록", description = "일반 사용자가 판매자로 등록합니다")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "409", description = "이미 판매자로 등록되어 있거나 사업자 번호 중복", content = @Content)
    public ResponseEntity<SellerResponse> registerSeller(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody SellerRequest request
    ) {
        log.info("POST /api/sellers - email: {}", email);
        SellerResponse response = sellerService.registerSeller(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "내 판매자 정보 조회", description = "현재 로그인한 판매자의 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음 (판매자 아님)", content = @Content)
    @ApiResponse(responseCode = "404", description = "판매자 정보를 찾을 수 없음", content = @Content)
    public ResponseEntity<SellerResponse> getMySeller(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("GET /api/sellers/me - email: {}", email);
        SellerResponse response = sellerService.getMySeller(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "내 판매자 정보 수정", description = "현재 로그인한 판매자의 정보를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "판매자 정보를 찾을 수 없음", content = @Content)
    @ApiResponse(responseCode = "409", description = "사업자 번호 중복", content = @Content)
    public ResponseEntity<SellerResponse> updateMySeller(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody SellerRequest request
    ) {
        log.info("PUT /api/sellers/me - email: {}", email);
        SellerResponse response = sellerService.updateMySeller(email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "판매자 등록 해제", description = "판매자 등록을 해제하고 일반 사용자로 전환합니다")
    @ApiResponse(responseCode = "204", description = "해제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "판매자 정보를 찾을 수 없음", content = @Content)
    public ResponseEntity<Void> deleteMySeller(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("DELETE /api/sellers/me - email: {}", email);
        sellerService.deleteMySeller(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/dashboard")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "판매자 대시보드 조회", description = "판매자의 통계 대시보드를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "판매자 정보를 찾을 수 없음", content = @Content)
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("GET /api/sellers/me/dashboard - email: {}", email);
        DashboardResponse response = dashboardService.getDashboard(email);
        return ResponseEntity.ok(response);
    }
}