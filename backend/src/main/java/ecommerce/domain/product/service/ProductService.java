package ecommerce.domain.product.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ForbiddenException;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.category.entity.Category;
import ecommerce.domain.category.repository.CategoryRepository;
import ecommerce.domain.product.dto.*;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.product.repository.ProductSpecification;
import ecommerce.domain.review.dto.ReviewResponse;
import ecommerce.domain.review.entity.Review;
import ecommerce.domain.review.repository.ReviewRepository;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.infrastructure.naver.NaverShoppingApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final NaverShoppingApiClient naverShoppingApiClient;

    /**
     * 상품 생성
     */
    @Transactional
    public ProductResponse createProduct(String email, ProductRequest request) {
        log.info("상품 생성 시도: email={}, name={}", email, request.getName());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 판매자 조회
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 카테고리 조회 (선택사항)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        // 상품 생성
        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .naverProductId(request.getNaverProductId())
                .status(request.getStock() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("상품 생성 완료: productId={}", savedProduct.getId());

        return ProductResponse.from(savedProduct);
    }

    /**
     * 상품 목록 조회 (검색/정렬/페이징)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(ProductSearchRequest searchRequest) {
        log.info("상품 목록 조회: keyword={}, categoryId={}",
                searchRequest.getKeyword(), searchRequest.getCategoryId());

        // 페이지 설정
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 20;

        // 정렬 설정
        Sort sort = createSort(searchRequest);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 동적 쿼리 생성
        Specification<Product> spec = ProductSpecification.searchProducts(searchRequest);

        // 조회
        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("상품 상세 조회: productId={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public ProductResponse updateProduct(String email, Long id, ProductRequest request) {
        log.info("상품 수정: email={}, productId={}", email, id);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 판매자 조회
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 본인 상품인지 확인
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 상품만 수정할 수 있습니다");
        }

        // 변경 사항 적용
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
            // 재고에 따른 상태 변경
            product.setStatus(request.getStock() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정 완료: productId={}", updatedProduct.getId());

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(String email, Long id) {
        log.info("상품 삭제: email={}, productId={}", email, id);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 판매자 조회
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 본인 상품인지 확인
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 상품만 삭제할 수 있습니다");
        }

        productRepository.delete(product);
        log.info("상품 삭제 완료: productId={}", id);
    }

    /**
     * 재고 업데이트 (재입고 이벤트 발행 포함)
     */
    @Transactional
    public ProductResponse updateStock(String email, Long id, StockUpdateRequest request) {
        log.info("재고 업데이트: email={}, productId={}, quantity={}", email, id, request.getQuantity());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 판매자 조회
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 본인 상품인지 확인
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED, "본인의 상품만 재고를 수정할 수 있습니다");
        }

        // 이전 재고 저장 (재입고 이벤트 판단용)
        int previousStock = product.getStock();

        // 재고 업데이트
        product.setStock(request.getQuantity());
        product.setStatus(request.getQuantity() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);

        Product updatedProduct = productRepository.save(product);

        // 재입고 이벤트 발행 (0 → 1+ 변경 시)
        if (previousStock == 0 && request.getQuantity() > 0) {
            log.info("재입고 이벤트 발행: productId={}, previousStock={}, newStock={}",
                    id, previousStock, request.getQuantity());
            // TODO: Phase 7에서 ProductRestockedEvent 발행 구현
            // applicationEventPublisher.publishEvent(new ProductRestockedEvent(id, previousStock, request.getQuantity()));
        }

        log.info("재고 업데이트 완료: productId={}, newStock={}", updatedProduct.getId(), updatedProduct.getStock());

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 네이버 쇼핑 API 검색
     */
    public NaverProductSearchResponse searchNaverProducts(String keyword, Integer display, Integer start) {
        log.info("네이버 쇼핑 API 검색: keyword={}, display={}, start={}", keyword, display, start);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다");
        }

        return naverShoppingApiClient.searchProducts(keyword, display, start);
    }

    /**
     * 상품 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        log.info("상품 리뷰 목록 조회: productId={}, page={}", productId, pageable.getPageNumber());

        // 상품 존재 확인
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 리뷰 조회
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 정렬 조건 생성
     */
    private Sort createSort(ProductSearchRequest searchRequest) {
        String sortField = searchRequest.getSort() != null ? searchRequest.getSort() : "createdAt";
        String direction = searchRequest.getDirection() != null ? searchRequest.getDirection() : "DESC";

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(sortDirection, sortField);
    }
}