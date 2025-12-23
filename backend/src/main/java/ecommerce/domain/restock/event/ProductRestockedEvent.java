package ecommerce.domain.restock.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRestockedEvent {
    private Long productId;
    private Integer previousStock;
    private Integer currentStock;
}