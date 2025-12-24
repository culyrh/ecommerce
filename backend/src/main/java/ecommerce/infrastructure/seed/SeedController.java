package ecommerce.infrastructure.seed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/seed")
@Tag(name = "Seed", description = "시드 데이터 관리")
public class SeedController {

    private final SeedDataService seedDataService;

    @PostMapping
    @Operation(
            summary = "시드 데이터 생성",
            description = """
            ⚠ JCloud 서버의 TLS outbound 제약으로 네이버 쇼핑 API 호출 불가
            
            - 배포 전에 로컬에서 생성한 JSON 파일(naver_products.json)을 적용
            """
    )
    public ResponseEntity<Map<String, String>> seed() {
        // 배포 환경 체크
        String profile = System.getenv("SPRING_PROFILES_ACTIVE");
        boolean isProduction = "prod".equals(profile) || "production".equals(profile);

        if (isProduction) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "disabled");
            response.put("message", "배포 환경에서는 비활성화됨");
            response.put("reason", "JCloud TLS outbound 제약으로 네이버 API 호출 불가");

            log.warn("배포 환경에서 시드 API 실행 시도 차단");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        // 로컬 환경에서만 실행
        try {
            seedDataService.generateSeedData();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "시드 데이터 생성 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("시드 데이터 생성 실패", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}