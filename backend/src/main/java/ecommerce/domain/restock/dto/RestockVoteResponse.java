package ecommerce.domain.restock.dto;

import ecommerce.domain.product.dto.ProductResponse;
import ecommerce.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "재입고 투표 응답")
public class RestockVoteResponse {

    @Schema(description = "투표 ID", example = "1")
    private Long id;

    @Schema(description = "상품 정보")
    private ProductResponse product;

    @Schema(description = "사용자 정보")
    private UserResponse user;

    @Schema(description = "투표 생성일시")
    private LocalDateTime createdAt;
}