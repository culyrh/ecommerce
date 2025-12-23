package ecommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotEmpty(message = "주문 상품 목록은 필수입니다")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "수령인 이름은 필수입니다")
    @Size(min = 2, max = 100, message = "수령인 이름은 2~100자 사이여야 합니다")
    private String recipientName;

    @NotBlank(message = "수령인 전화번호는 필수입니다")
    private String recipientPhone;

    @NotBlank(message = "배송 주소는 필수입니다")
    @Size(max = 500, message = "배송 주소는 500자를 초과할 수 없습니다")
    private String address;

    private Long couponId;
}