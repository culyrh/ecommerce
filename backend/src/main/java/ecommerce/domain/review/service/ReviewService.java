package ecommerce.domain.review.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ForbiddenException;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.order.entity.OrderItem;
import ecommerce.domain.order.repository.OrderItemRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.review.dto.ReviewRequest;
import ecommerce.domain.review.dto.ReviewResponse;
import ecommerce.domain.review.entity.Review;
import ecommerce.domain.review.repository.ReviewRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 리뷰 작성
     */
    @Transactional
    public ReviewResponse createReview(String email, ReviewRequest request) {
        log.info("리뷰 작성 시도: email={}, productId={}, rating={}",
                email, request.getProductId(), request.getRating());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 주문 항목 조회 (선택사항)
        OrderItem orderItem = null;
        if (request.getOrderItemId() != null) {
            orderItem = orderItemRepository.findById(request.getOrderItemId())
                    .orElse(null);
        }

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 작성 완료: reviewId={}", savedReview.getId());

        return ReviewResponse.from(savedReview);
    }

    /**
     * 내 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(String email, Pageable pageable) {
        log.info("내 리뷰 목록 조회: email={}, page={}", email, pageable.getPageNumber());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByUserId(user.getId(), pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponse updateReview(String email, Long id, ReviewRequest request) {
        log.info("리뷰 수정: email={}, reviewId={}", email, id);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 확인
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 리뷰만 수정할 수 있습니다");
        }

        // 변경 사항 적용
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        Review updatedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료: reviewId={}", updatedReview.getId());

        return ReviewResponse.from(updatedReview);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(String email, Long id) {
        log.info("리뷰 삭제: email={}, reviewId={}", email, id);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 확인
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 리뷰만 삭제할 수 있습니다");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료: reviewId={}", id);
    }
}