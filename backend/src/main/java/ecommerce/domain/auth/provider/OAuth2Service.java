package ecommerce.domain.auth.provider;

import ecommerce.common.enums.Role;
import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.UnauthorizedException;
import ecommerce.domain.auth.dto.OAuth2UserInfo;
import ecommerce.domain.auth.dto.TokenResponse;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    /**
     * Naver OAuth2 로그인
     */
    @Transactional
    public TokenResponse naverLogin(String code, String state) {
        log.info("Naver 로그인 시도: code={}, state={}", code, state);

        try {
            // 1. Authorization Code로 Access Token 획득
            String accessToken = getNaverAccessToken(code, state);

            // 2. Access Token으로 사용자 정보 조회
            OAuth2UserInfo userInfo = getNaverUserInfo(accessToken);

            // 3. 사용자 등록 또는 조회
            User user = findOrCreateUser(userInfo);

            log.info("Naver 로그인 성공: userId={}, email={}, roles={}",
                    user.getId(), user.getEmail(), user.getRoles());

            // 4. JWT 토큰 발급
            return createTokenResponse(user);

        } catch (Exception e) {
            log.error("Naver 로그인 실패", e);
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Naver 로그인에 실패했습니다");
        }
    }

    /**
     * Naver Access Token 획득
     */
    private String getNaverAccessToken(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        String body = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&state=%s",
                naverClientId, naverClientSecret, code, state
        );

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Access Token 획득 실패");
            }

            return (String) responseBody.get("access_token");

        } catch (Exception e) {
            log.error("Naver Access Token 획득 실패", e);
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Access Token 획득에 실패했습니다");
        }
    }

    /**
     * Naver 사용자 정보 조회
     */
    private OAuth2UserInfo getNaverUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("response")) {
                throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "사용자 정보 조회 실패");
            }

            Map<String, Object> naverAccount = (Map<String, Object>) responseBody.get("response");

            return OAuth2UserInfo.builder()
                    .provider("naver")
                    .providerId((String) naverAccount.get("id"))
                    .email((String) naverAccount.get("email"))
                    .name((String) naverAccount.get("name"))
                    .build();

        } catch (Exception e) {
            log.error("Naver 사용자 정보 조회 실패", e);
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "사용자 정보 조회에 실패했습니다");
        }
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        // Provider와 ProviderId로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                userInfo.getProvider(),
                userInfo.getProviderId()
        );

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // 이메일로 기존 계정 확인
        Optional<User> userByEmail = userRepository.findByEmail(userInfo.getEmail());
        if (userByEmail.isPresent()) {
            // 기존 계정에 소셜 로그인 정보 연동
            User user = userByEmail.get();
            user.setProvider(userInfo.getProvider());
            user.setProviderId(userInfo.getProviderId());
            return userRepository.save(user);
        }

        // 신규 사용자 생성
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .isActive(true)
                .build();

        // ROLE_USER는 @PrePersist에서 자동으로 추가됨

        return userRepository.save(newUser);
    }

    /**
     * JWT 토큰 응답 생성
     */
    private TokenResponse createTokenResponse(User user) {
        // Set<Role>을 콤마로 구분된 문자열로 변환
        String roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                roles
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }
}