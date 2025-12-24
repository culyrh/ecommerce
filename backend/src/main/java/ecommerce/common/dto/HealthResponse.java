package ecommerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 헬스체크 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {

    /**
     * 서버 상태 (UP/DOWN)
     */
    private String status;

    /**
     * 애플리케이션 버전
     */
    private String version;

    /**
     * 빌드 시간
     */
    private LocalDateTime buildTime;

    /**
     * 서버 가동 시간 (예: "1d 2h 30m")
     */
    private String uptime;

    /**
     * 데이터베이스 상태 (UP/DOWN)
     */
    private String database;

    /**
     * Redis 상태 (UP/DOWN)
     */
    private String redis;
}