package ecommerce.domain.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "상품명은 필수입니다")
    @Size(min = 2, max = 255, message = "상품명은 2~255자 사이여야 합니다")
    private String name;

    @Size(max = 5000, message = "상품 설명은 5000자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    @Digits(integer = 10, fraction = 2, message = "가격 형식이 올바르지 않습니다")
    private BigDecimal price;

    @NotNull(message = "재고는 필수입니다")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer stock;

    private Long categoryId;

    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
    private String imageUrl;

    @Size(max = 100, message = "네이버 상품 ID는 100자를 초과할 수 없습니다")
    private String naverProductId;
}