package ecommerce.domain.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // 오늘의 통계
    private Long todayOrderCount;
    private BigDecimal todayRevenue;

    // 상품별 판매 순위
    private List<ProductSalesDto> topSellingProducts;

    // 재고 부족 상품
    private List<LowStockProductDto> lowStockProducts;

    // 리뷰 통계
    private ReviewStatsDto reviewStats;

    // 매출 그래프 데이터
    private List<DailySalesDto> salesChart;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ProductSalesDto {
        private Long productId;
        private String productName;
        private Long salesCount;
        private BigDecimal revenue;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class LowStockProductDto {
        private Long productId;
        private String productName;
        private Integer currentStock;
        private Integer minStockThreshold;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ReviewStatsDto {
        private Double averageRating;
        private Long totalReviews;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class DailySalesDto {
        private LocalDate date;
        private Long orderCount;
        private BigDecimal revenue;
    }
}