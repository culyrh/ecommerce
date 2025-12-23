package ecommerce.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProductSearchResponse {

    private String lastBuildDate;
    private Integer total;
    private Integer start;
    private Integer display;
    private List<NaverProduct> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverProduct {
        private String title;
        private String link;
        private String image;
        private String lprice; // 최저가
        private String hprice; // 최고가
        private String mallName;
        private String productId;
        private String productType;
        private String brand;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
    }
}