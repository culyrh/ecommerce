package ecommerce.domain.user.controller;

import ecommerce.domain.user.dto.UserResponse;
import ecommerce.domain.user.dto.UserUpdateRequest;
import ecommerce.domain.user.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "회원 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    public ResponseEntity<UserResponse> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("GET /api/users/me - email: {}", email);
        UserResponse response = userService.getMyInfo(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    public ResponseEntity<UserResponse> updateMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal String email,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("PUT /api/users/me - email: {}", email);
        UserResponse response = userService.updateMyInfo(email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 비활성화합니다 (소프트 삭제)")
    @ApiResponse(responseCode = "204", description = "탈퇴 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    public ResponseEntity<Void> deleteMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal String email
    ) {
        log.info("DELETE /api/users/me - email: {}", email);
        userService.deleteMyAccount(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "회원 목록 조회 (관리자 전용)",
            description = "전체 회원 목록을 페이지네이션하여 조회합니다"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("GET /api/users - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }
}