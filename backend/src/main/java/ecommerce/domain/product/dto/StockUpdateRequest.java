package ecommerce.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {

    @NotNull(message = "재고 수량은 필수입니다")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer quantity;
}