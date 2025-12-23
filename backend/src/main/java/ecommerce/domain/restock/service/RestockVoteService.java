package ecommerce.domain.restock.service;

import ecommerce.common.exception.BadRequestException;
import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.notification.entity.Notification;
import ecommerce.domain.notification.enums.NotificationType;
import ecommerce.domain.notification.repository.NotificationRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.dto.RestockVoteRequest;
import ecommerce.domain.restock.dto.RestockVoteResponse;
import ecommerce.domain.restock.entity.RestockVote;
import ecommerce.domain.restock.repository.RestockVoteRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestockVoteService {

    private final RestockVoteRepository restockVoteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;
    private final RedisService redisService;

    private static final String VOTE_COUNT_KEY_PREFIX = "restock:vote:count:";
    private static final int VOTE_THRESHOLD = 50;

    @Transactional
    public RestockVoteResponse voteForRestock(String email, RestockVoteRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 중복 투표 체크
        if (restockVoteRepository.existsByUserAndProduct(user, product)) {
            throw new BadRequestException(ErrorCode.DUPLICATE_VOTE);
        }

        // 투표 생성
        RestockVote vote = RestockVote.builder()
                .user(user)
                .product(product)
                .build();

        RestockVote savedVote = restockVoteRepository.save(vote);

        // Redis 카운트 증가
        String countKey = VOTE_COUNT_KEY_PREFIX + product.getId();
        Long voteCount = redisService.increment(countKey);

        log.info("재입고 투표 생성: 사용자={}, 상품={}, 현재 투표수={}", email, product.getName(), voteCount);

        // 임계값 체크
        if (voteCount != null && voteCount >= VOTE_THRESHOLD) {
            createAdminNotification(product, voteCount.intValue());
        }

        return mapToResponse(savedVote);
    }

    public Page<RestockVoteResponse> getMyVotes(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        return restockVoteRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponse);
    }

    public Page<RestockVoteResponse> getProductVotes(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return restockVoteRepository.findByProductOrderByCreatedAtDesc(product, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void cancelVote(String email, Long voteId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        RestockVote vote = restockVoteRepository.findById(voteId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOTE_NOT_FOUND));

        // 본인 투표인지 확인
        if (!vote.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.FORBIDDEN);
        }

        // Redis 카운트 감소
        String countKey = VOTE_COUNT_KEY_PREFIX + vote.getProduct().getId();
        redisService.decrement(countKey);

        restockVoteRepository.delete(vote);
        log.info("재입고 투표 취소: 사용자={}, 투표ID={}", email, voteId);
    }

    private void createAdminNotification(Product product, int voteCount) {
        // 기존 알림이 있는지 확인 (중복 방지)
        String redisKey = "restock:admin:notified:" + product.getId();
        if (redisService.getValue(redisKey) != null) {
            return;
        }

        // 관리자용 더미 사용자 조회 또는 생성 (admin@system.com)
        // 또는 user를 null로 허용하도록 Notification 엔티티 수정 필요
        // 현재는 임시로 첫 번째 관리자 사용자를 조회
        User adminUser = userRepository.findById(1L).orElse(null);

        if (adminUser == null) {
            log.warn("관리자 사용자를 찾을 수 없습니다. 알림 생성 건너뜀");
            return;
        }

        // 관리자 알림 생성
        String message = String.format("상품 '%s'에 대한 재입고 투표가 %d개에 도달했습니다.",
                product.getName(), voteCount);

        Notification notification = Notification.builder()
                .user(adminUser)  // 수정: userId 대신 user 객체 사용
                .type(NotificationType.RESTOCK)
                .title("재입고 투표 임계값 도달")
                .content(message)  // 수정: message 대신 content 필드 사용
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // Redis에 알림 발송 플래그 저장 (24시간 TTL)
        redisService.setValue(redisKey, "true", 86400L);

        log.info("관리자 알림 생성: 상품={}, 투표수={}", product.getName(), voteCount);
    }

    private RestockVoteResponse mapToResponse(RestockVote vote) {
        return RestockVoteResponse.builder()
                .id(vote.getId())
                .product(mapToProductResponse(vote.getProduct()))
                .user(mapToUserResponse(vote.getUser()))
                .createdAt(vote.getCreatedAt())
                .build();
    }

    private ecommerce.domain.product.dto.ProductResponse mapToProductResponse(Product product) {
        return ecommerce.domain.product.dto.ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .build();
    }

    private ecommerce.domain.user.dto.UserResponse mapToUserResponse(User user) {
        return ecommerce.domain.user.dto.UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}