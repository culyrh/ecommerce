package ecommerce.domain.category.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.category.dto.CategoryRequest;
import ecommerce.domain.category.dto.CategoryResponse;
import ecommerce.domain.category.entity.Category;
import ecommerce.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("카테고리 생성 시도: name={}, parentId={}", request.getName(), request.getParentId());

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "부모 카테고리를 찾을 수 없습니다"));
        }

        Category category = Category.builder()
                .name(request.getName())
                .parent(parent)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: categoryId={}", savedCategory.getId());

        return CategoryResponse.fromWithoutChildren(savedCategory);
    }

    /**
     * 전체 카테고리 목록 조회 (계층 구조)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("전체 카테고리 조회");

        // 최상위 카테고리만 조회 (parent_id가 null인 것들)
        List<Category> rootCategories = categoryRepository.findByParentIsNull();

        // 계층 구조로 변환
        return rootCategories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("카테고리 수정: categoryId={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        // 이름 변경
        if (request.getName() != null) {
            category.setName(request.getName());
        }

        // 부모 카테고리 변경
        if (request.getParentId() != null) {
            // 자기 자신을 부모로 설정하는 것 방지
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("자기 자신을 부모 카테고리로 설정할 수 없습니다");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "부모 카테고리를 찾을 수 없습니다"));

            category.setParent(parent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            // parentId가 명시적으로 null이면 최상위 카테고리로 변경
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("카테고리 수정 완료: categoryId={}", updatedCategory.getId());

        return CategoryResponse.fromWithoutChildren(updatedCategory);
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("카테고리 삭제: categoryId={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        // 하위 카테고리가 있는지 확인
        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("하위 카테고리가 존재하는 카테고리는 삭제할 수 없습니다");
        }

        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료: categoryId={}", id);
    }
}