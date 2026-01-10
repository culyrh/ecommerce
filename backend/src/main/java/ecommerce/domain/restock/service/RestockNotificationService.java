package ecommerce.domain.restock.service;

import ecommerce.common.exception.BadRequestException;
import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.restock.dto.RestockNotificationRequest;
import ecommerce.domain.restock.dto.RestockNotificationResponse;
import ecommerce.domain.restock.entity.RestockNotification;
import ecommerce.domain.restock.repository.RestockNotificationRepository;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestockNotificationService {

    private final RestockNotificationRepository restockNotificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public RestockNotificationResponse subscribeRestock(String email, RestockNotificationRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 기존 알림 신청 확인
        Optional<RestockNotification> existingNotification =
                restockNotificationRepository.findByProductIdAndUserId(product.getId(), user.getId());

        if (existingNotification.isPresent()) {
            RestockNotification notification = existingNotification.get();

            // 이미 발송되지 않은 알림이 있으면 중복 에러
            if (!notification.getIsNotified()) {
                throw new BadRequestException(ErrorCode.DUPLICATE_NOTIFICATION_REQUEST);
            }

            // 이전에 발송된 알림이 있으면 재사용 (isNotified를 false로 초기화)
            notification.setIsNotified(false);
            RestockNotification savedNotification = restockNotificationRepository.save(notification);

            log.info("재입고 알림 재신청: 사용자={}, 상품={}", email, product.getName());
            return mapToResponse(savedNotification);
        }

        // 새로운 알림 신청 생성
        RestockNotification notification = RestockNotification.builder()
                .user(user)
                .product(product)
                .isNotified(false)
                .build();

        RestockNotification savedNotification = restockNotificationRepository.save(notification);

        log.info("재입고 알림 신청: 사용자={}, 상품={}", email, product.getName());

        return mapToResponse(savedNotification);
    }

    public Page<RestockNotificationResponse> getMySubscriptions(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        return restockNotificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponse);
    }

    public Page<RestockNotificationResponse> getProductSubscriptions(String email, Long productId, Pageable pageable) {
        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 수정: findByUser 대신 findByUserId 사용
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 본인 상품인지 확인
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BadRequestException(ErrorCode.FORBIDDEN);
        }

        return restockNotificationRepository.findByProductOrderByCreatedAtDesc(product, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void unsubscribeRestock(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        RestockNotification notification = restockNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 본인 신청인지 확인
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.FORBIDDEN);
        }

        restockNotificationRepository.delete(notification);
        log.info("재입고 알림 취소: 사용자={}, 알림ID={}", email, notificationId);
    }

    private RestockNotificationResponse mapToResponse(RestockNotification notification) {
        return RestockNotificationResponse.builder()
                .id(notification.getId())
                .product(mapToProductResponse(notification.getProduct()))
                .user(mapToUserResponse(notification.getUser()))
                .isNotified(notification.getIsNotified())
                .createdAt(notification.getCreatedAt())
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