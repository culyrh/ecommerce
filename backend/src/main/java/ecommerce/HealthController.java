package ecommerce;

import ecommerce.common.dto.HealthResponse;
import ecommerce.infrastructure.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스체크 컨트롤러
 * 서버 상태, DB 연결, Redis 연결 등을 확인
 */
@Tag(name = "Health Check", description = "헬스체크 API")
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisService redisService;

    private static final LocalDateTime BUILD_TIME = LocalDateTime.of(2024, 12, 24, 10, 0, 0);
    private static final String VERSION = "1.0.0";

    /**
     * 홈 엔드포인트
     */
    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "E-commerce Platform is running");
        response.put("version", VERSION);
        return response;
    }

    /**
     * 헬스체크 엔드포인트
     * 인증 없이 접근 가능하며, 서버, DB, Redis 상태를 확인
     */
    @Operation(
            summary = "헬스체크",
            description = "서버 상태, 데이터베이스 연결, Redis 연결 상태를 확인합니다."
    )
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        String databaseStatus = checkDatabase();
        String redisStatus = checkRedis();

        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .version(VERSION)
                .buildTime(BUILD_TIME)
                .uptime(getUptime())
                .database(databaseStatus)
                .redis(redisStatus)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 데이터베이스 연결 확인
     */
    private String checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    /**
     * Redis 연결 확인
     */
    private String checkRedis() {
        try {
            redisService.setValue("health:check", "OK");
            String value = redisService.getStringValue("health:check");
            return "OK".equals(value) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    /**
     * 서버 가동 시간 계산
     */
    private String getUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptimeMillis);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return String.format("%dd %dh %dm", days, hours, minutes);
    }
}