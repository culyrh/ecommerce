package ecommerce.domain.restock.controller;

import ecommerce.domain.restock.dto.RestockVoteRequest;
import ecommerce.domain.restock.dto.RestockVoteResponse;
import ecommerce.domain.restock.service.RestockVoteService;
import ecommerce.infrastructure.redis.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Restock Votes", description = "재입고 투표 API")
@RestController
@RequestMapping("/api/restock-votes")
@RequiredArgsConstructor
public class RestockVoteController {

    private final RestockVoteService restockVoteService;
    private final RedisService redisService;

    private static final String VOTE_COUNT_KEY_PREFIX = "restock:vote:count:";

    @Operation(summary = "재입고 투표", description = "상품에 대한 재입고 투표를 생성합니다")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RestockVoteResponse> voteForRestock(
            @Valid @RequestBody RestockVoteRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        RestockVoteResponse response = restockVoteService.voteForRestock(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 투표 목록 조회", description = "본인이 한 재입고 투표 목록을 조회합니다")
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<RestockVoteResponse>> getMyVotes(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        String email = authentication.getName();
        Page<RestockVoteResponse> votes = restockVoteService.getMyVotes(email, pageable);
        return ResponseEntity.ok(votes);
    }

    @Operation(summary = "상품별 투표 목록 조회", description = "특정 상품에 대한 재입고 투표 목록을 조회합니다")
    @GetMapping("/products/{productId}")
    public ResponseEntity<Page<RestockVoteResponse>> getProductVotes(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RestockVoteResponse> votes = restockVoteService.getProductVotes(productId, pageable);
        return ResponseEntity.ok(votes);
    }

    /**
     * 추가: Redis 기반 실시간 투표수 조회 API
     * 프론트엔드에서 DB 대신 Redis에서 직접 투표수를 조회할 수 있도록 제공
     * 재입고 시 즉시 0으로 반영됨
     */
    @Operation(summary = "상품별 투표수 조회 (Redis)",
            description = "Redis에서 실시간 재입고 투표수를 조회합니다. 재입고 시 즉시 0으로 초기화됩니다.")
    @GetMapping("/products/{productId}/count")
    public ResponseEntity<Long> getVoteCount(@PathVariable Long productId) {
        String countKey = VOTE_COUNT_KEY_PREFIX + productId;
        Object countValue = redisService.getValue(countKey);

        Long count = 0L;

        if (countValue != null) {
            // Redis에서 가져온 값의 타입에 따라 처리
            if (countValue instanceof String) {
                try {
                    count = Long.parseLong((String) countValue);
                } catch (NumberFormatException e) {
                    count = 0L;
                }
            } else if (countValue instanceof Long) {
                count = (Long) countValue;
            } else if (countValue instanceof Integer) {
                count = ((Integer) countValue).longValue();
            }
        }

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "투표 취소", description = "재입고 투표를 취소합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelVote(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        restockVoteService.cancelVote(email, id);
        return ResponseEntity.noContent().build();
    }
}