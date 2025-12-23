package ecommerce.domain.order.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdateRequest {

    @Size(min = 2, max = 100, message = "수령인 이름은 2~100자 사이여야 합니다")
    private String recipientName;

    private String recipientPhone;

    @Size(max = 500, message = "배송 주소는 500자를 초과할 수 없습니다")
    private String address;
}