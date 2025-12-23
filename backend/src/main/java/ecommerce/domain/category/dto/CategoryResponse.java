package ecommerce.domain.category.dto;

import ecommerce.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryResponse> children;
    private LocalDateTime createdAt;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(category.getChildren() != null
                        ? category.getChildren().stream()
                        .map(CategoryResponse::from)
                        .collect(Collectors.toList())
                        : List.of())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public static CategoryResponse fromWithoutChildren(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(List.of())
                .createdAt(category.getCreatedAt())
                .build();
    }
}