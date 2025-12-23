package ecommerce.domain.seller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRequest {

    @NotBlank(message = "사업자명은 필수입니다")
    @Size(min = 2, max = 255, message = "사업자명은 2~255자 사이여야 합니다")
    private String businessName;

    @NotBlank(message = "사업자 번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "올바른 사업자 번호 형식이 아닙니다 (예: 123-45-67890)")
    private String businessNumber;

    @Min(value = 0, message = "최소 재고 임계값은 0 이상이어야 합니다")
    private Integer minStockThreshold;
}