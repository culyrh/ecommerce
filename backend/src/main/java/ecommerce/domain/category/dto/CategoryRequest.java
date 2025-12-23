package ecommerce.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(min = 2, max = 100, message = "카테고리명은 2~100자 사이여야 합니다")
    private String name;

    private Long parentId;
}