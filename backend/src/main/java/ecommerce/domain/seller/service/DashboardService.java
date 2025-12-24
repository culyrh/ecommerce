package ecommerce.domain.seller.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.order.entity.Order;
import ecommerce.domain.order.entity.OrderItem;
import ecommerce.domain.order.repository.OrderItemRepository;
import ecommerce.domain.order.repository.OrderRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.review.entity.Review;
import ecommerce.domain.review.repository.ReviewRepository;
import ecommerce.domain.seller.dto.DashboardResponse;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.infrastructure.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final RedisService redisService;

    private static final String REDIS_DAILY_STATS_PREFIX = "seller:stats:daily:";

    /**
     * 판매자 대시보드 조회
     */
    public DashboardResponse getDashboard(String email) {
        log.info("대시보드 조회 시작: email={}", email);

        // User 조회 후 Seller 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 1. 오늘의 주문 수 및 매출
        Long todayOrderCount = getTodayOrderCount(seller.getId());
        BigDecimal todayRevenue = getTodayRevenue(seller.getId());

        // 2. 상품별 판매 순위 (최근 30일)
        List<DashboardResponse.ProductSalesDto> topSellingProducts = getTopSellingProducts(seller.getId());

        // 3. 재고 부족 상품
        List<DashboardResponse.LowStockProductDto> lowStockProducts = getLowStockProducts(seller);

        // 4. 리뷰 통계
        DashboardResponse.ReviewStatsDto reviewStats = getReviewStats(seller.getId());

        // 5. 일별 매출 데이터 (최근 30일)
        List<DashboardResponse.DailySalesDto> salesChart = getDailySalesChart(seller.getId());

        log.info("대시보드 조회 완료: sellerId={}", seller.getId());

        return DashboardResponse.builder()
                .todayOrderCount(todayOrderCount)
                .todayRevenue(todayRevenue)
                .topSellingProducts(topSellingProducts)
                .lowStockProducts(lowStockProducts)
                .reviewStats(reviewStats)
                .salesChart(salesChart)
                .build();
    }

    /**
     * 오늘의 주문 수 조회 (Redis 캐시 활용)
     */
    private Long getTodayOrderCount(Long sellerId) {
        String key = REDIS_DAILY_STATS_PREFIX + sellerId + ":" + LocalDate.now() + ":count";
        String cachedValue = redisService.getStringValue(key);

        if (cachedValue != null) {
            log.debug("Redis에서 오늘의 주문 수 조회: sellerId={}, count={}", sellerId, cachedValue);
            return Long.parseLong(cachedValue);
        }

        // Redis에 없으면 DB에서 조회
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<OrderItem> todayItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getSeller().getId().equals(sellerId))
                .filter(item -> item.getCreatedAt().isAfter(startOfDay) && item.getCreatedAt().isBefore(endOfDay))
                .collect(Collectors.toList());

        long count = todayItems.stream()
                .map(OrderItem::getOrder)
                .distinct()
                .count();

        // Redis에 저장 (TTL 24시간)
        redisService.setValue(key, String.valueOf(count), 86400L);
        log.debug("DB에서 오늘의 주문 수 조회 후 캐싱: sellerId={}, count={}", sellerId, count);

        return count;
    }

    /**
     * 오늘의 매출 조회 (Redis 캐시 활용)
     */
    private BigDecimal getTodayRevenue(Long sellerId) {
        String key = REDIS_DAILY_STATS_PREFIX + sellerId + ":" + LocalDate.now() + ":revenue";
        String cachedValue = redisService.getStringValue(key);

        if (cachedValue != null) {
            log.debug("Redis에서 오늘의 매출 조회: sellerId={}, revenue={}", sellerId, cachedValue);
            return new BigDecimal(cachedValue);
        }

        // Redis에 없으면 DB에서 조회
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        BigDecimal revenue = orderItemRepository.findAll().stream()
                .filter(item -> item.getSeller().getId().equals(sellerId))
                .filter(item -> item.getCreatedAt().isAfter(startOfDay) && item.getCreatedAt().isBefore(endOfDay))
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Redis에 저장 (TTL 24시간)
        redisService.setValue(key, revenue.toString(), 86400L);
        log.debug("DB에서 오늘의 매출 조회 후 캐싱: sellerId={}, revenue={}", sellerId, revenue);

        return revenue;
    }

    /**
     * 상품별 판매 순위 조회 (최근 30일)
     */
    private List<DashboardResponse.ProductSalesDto> getTopSellingProducts(Long sellerId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<OrderItem> recentItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getSeller().getId().equals(sellerId))
                .filter(item -> item.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        Map<Product, List<OrderItem>> groupedByProduct = recentItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getProduct));

        return groupedByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<OrderItem> items = entry.getValue();

                    long salesCount = items.stream()
                            .mapToLong(OrderItem::getQuantity)
                            .sum();

                    BigDecimal revenue = items.stream()
                            .map(OrderItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DashboardResponse.ProductSalesDto.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .salesCount(salesCount)
                            .revenue(revenue)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getSalesCount(), a.getSalesCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 재고 부족 상품 조회
     */
    private List<DashboardResponse.LowStockProductDto> getLowStockProducts(Seller seller) {
        Integer threshold = seller.getMinStockThreshold() != null ? seller.getMinStockThreshold() : 10;

        List<Product> lowStockProducts = productRepository.findBySellerId(seller.getId()).stream()
                .filter(p -> p.getStock() < threshold)
                .collect(Collectors.toList());

        return lowStockProducts.stream()
                .map(product -> DashboardResponse.LowStockProductDto.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .currentStock(product.getStock())
                        .minStockThreshold(threshold)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 리뷰 통계 조회
     */
    private DashboardResponse.ReviewStatsDto getReviewStats(Long sellerId) {
        List<Product> sellerProducts = productRepository.findBySellerId(sellerId);
        List<Long> productIds = sellerProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(review -> productIds.contains(review.getProduct().getId()))
                .collect(Collectors.toList());

        double averageRating = reviews.isEmpty() ? 0.0 :
                reviews.stream()
                        .mapToDouble(Review::getRating)
                        .average()
                        .orElse(0.0);

        long totalReviews = reviews.size();

        return DashboardResponse.ReviewStatsDto.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    /**
     * 일별 매출 차트 데이터 조회 (최근 30일)
     */
    private List<DashboardResponse.DailySalesDto> getDailySalesChart(Long sellerId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<DashboardResponse.DailySalesDto> salesChart = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            List<OrderItem> dayItems = orderItemRepository.findAll().stream()
                    .filter(item -> item.getSeller().getId().equals(sellerId))
                    .filter(item -> item.getCreatedAt().isAfter(startOfDay) && item.getCreatedAt().isBefore(endOfDay))
                    .collect(Collectors.toList());

            long orderCount = dayItems.stream()
                    .map(OrderItem::getOrder)
                    .distinct()
                    .count();

            BigDecimal revenue = dayItems.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            salesChart.add(DashboardResponse.DailySalesDto.builder()
                    .date(date)
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }

        return salesChart;
    }
}