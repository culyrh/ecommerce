package ecommerce.domain.auth.service;

import ecommerce.common.enums.Role;
import ecommerce.common.exception.DuplicateResourceException;
import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.UnauthorizedException;
import ecommerce.domain.auth.dto.LoginRequest;
import ecommerce.domain.auth.dto.RegisterRequest;
import ecommerce.domain.auth.dto.TokenResponse;
import ecommerce.domain.auth.provider.JwtTokenProvider;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        log.info("회원가입 시도: email={}", request.getEmail());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 성공: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", request.getEmail());

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS));

        // 활성 계정 확인
        if (!user.getIsActive()) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "비활성화된 계정입니다");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }
}