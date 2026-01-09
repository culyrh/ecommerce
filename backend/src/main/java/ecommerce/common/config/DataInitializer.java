package ecommerce.common.config;

import ecommerce.common.enums.Role;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("========================================");
        log.info("초기 데이터 로딩 시작");
        log.info("========================================");

        createAdminAccount();

        log.info("========================================");
        log.info("초기 데이터 로딩 완료");
        log.info("========================================");
    }

    /**
     * ADMIN 계정 생성
     */
    private void createAdminAccount() {
        String adminEmail = "admin@example.com";
        String adminPassword = "admin1234";

        // ADMIN 계정이 이미 존재하는지 확인
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ ADMIN 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // ADMIN 계정 생성
        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .phone("010-0000-0000")
                .isActive(true)
                .build();

        // ADMIN 역할 추가
        admin.addRole(Role.ROLE_ADMIN);

        userRepository.save(admin);

        log.info("========================================");
        log.info("✅ ADMIN 계정 생성 완료!");
        log.info("📧 Email: {}", adminEmail);
        log.info("🔑 Password: {}", adminPassword);
        log.info("⚠️  보안을 위해 운영 환경에서는 비밀번호를 변경하세요!");
        log.info("========================================");
    }
}