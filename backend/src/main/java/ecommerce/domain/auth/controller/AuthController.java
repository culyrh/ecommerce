package ecommerce.domain.auth.controller;

import ecommerce.domain.auth.dto.FirebaseLoginRequest;
import ecommerce.domain.auth.dto.LoginRequest;
import ecommerce.domain.auth.dto.RegisterRequest;
import ecommerce.domain.auth.dto.TokenResponse;
import ecommerce.domain.auth.provider.OAuth2Service;
import ecommerce.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oauth2Service;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "일반 회원가입 (이메일/비밀번호)")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content)
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("POST /api/auth/register - email: {}", request.getEmail());
        TokenResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT 토큰 발급")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)", content = @Content)
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/naver")
    @Operation(
            summary = "Naver 소셜 로그인",
            description = "Naver OAuth2 Authorization Code를 사용하여 로그인"
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "Naver 인증 실패", content = @Content)
    public ResponseEntity<TokenResponse> naverLogin(
            @Parameter(description = "Naver Authorization Code")
            @RequestParam String code,
            @Parameter(description = "CSRF 방지를 위한 state 파라미터")
            @RequestParam String state
    ) {
        log.info("GET /api/auth/naver - code: {}, state: {}", code, state);
        TokenResponse response = oauth2Service.naverLogin(code, state);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/firebase")
    @Operation(
            summary = "Firebase 소셜 로그인",
            description = "Firebase ID Token을 사용하여 로그인"
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "Firebase 인증 실패", content = @Content)
    public ResponseEntity<TokenResponse> firebaseLogin(
            @Valid @RequestBody FirebaseLoginRequest request
    ) {
        log.info("POST /api/auth/firebase");
        TokenResponse response = oauth2Service.firebaseLogin(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}