package ecommerce.domain.product.service;

import ecommerce.common.enums.Role;
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
     * 내 상품 목록 조회 (판매자)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyProducts(String email, Pageable pageable) {
        log.info("내 상품 목록 조회: email={}", email);

        // User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // Seller 조회
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // Seller의 상품 목록 조회
        Page<Product> products = productRepository.findBySellerId(seller.getId(), pageable);

        return products.map(ProductResponse::from);
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

        // 검색 조건 설정 (deprecated Specification.where() 대신 null 체크 방식 사용)
        Specification<Product> spec = null;

        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isBlank()) {
            spec = ProductSpecification.hasKeyword(searchRequest.getKeyword());
        }

        if (searchRequest.getCategoryId() != null) {
            Specification<Product> categorySpec = ProductSpecification.hasCategoryId(searchRequest.getCategoryId());
            spec = spec == null ? categorySpec : spec.and(categorySpec);
        }

        // 상품 조회
        Page<Product> products;
        if (spec != null) {
            products = productRepository.findAll(spec, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(ProductResponse::from);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        log.info("상품 상세 조회: productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public ProductResponse updateProduct(String email, Long productId, ProductRequest request) {
        log.info("상품 수정 시도: email={}, productId={}", email, productId);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 확인 (본인 상품인지 또는 관리자인지)
        if (!product.getSeller().getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        // 카테고리 업데이트
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        // 상품 정보 업데이트
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
            // 재고에 따라 상태 업데이트
            product.setStatus(request.getStock() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정 완료: productId={}", updatedProduct.getId());

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(String email, Long productId) {
        log.info("상품 삭제 시도: email={}, productId={}", email, productId);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 확인 (본인 상품인지 또는 관리자인지)
        if (!product.getSeller().getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        productRepository.delete(product);
        log.info("상품 삭제 완료: productId={}", productId);
    }

    /**
     * 재고 업데이트
     */
    @Transactional
    public ProductResponse updateStock(String email, Long productId, StockUpdateRequest request) {
        log.info("재고 업데이트: email={}, productId={}, newStock={}",
                email, productId, request.getQuantity());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 확인
        if (!product.getSeller().getUser().getId().equals(user.getId()) &&
                !user.getRoles().contains(Role.ROLE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN);
        }

        // 재고 업데이트
        product.setStock(request.getQuantity());
        product.setStatus(request.getQuantity() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);

        Product updatedProduct = productRepository.save(product);
        log.info("재고 업데이트 완료: productId={}, newStock={}", productId, request.getQuantity());

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 상품 리뷰 조회
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        log.info("상품 리뷰 조회: productId={}", productId);

        // 상품 존재 확인
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(ReviewResponse::from);
    }

    /**
     * 네이버 쇼핑 API로 상품 검색
     */
    public NaverProductSearchResponse searchNaverProducts(String query, int start, int display) {
        log.info("네이버 쇼핑 검색: query={}, start={}, display={}", query, start, display);
        return naverShoppingApiClient.searchProducts(query, start, display);
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