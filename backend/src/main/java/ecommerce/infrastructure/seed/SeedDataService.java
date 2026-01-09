package ecommerce.infrastructure.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.common.enums.Role;
import ecommerce.domain.category.entity.Category;
import ecommerce.domain.category.repository.CategoryRepository;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 파일 기반 시드 데이터 생성 서비스
 * resources/seed/naver_products.json 파일을 읽어서 DB에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    /**
     * JSON 파일에서 시드 데이터 로드 및 생성
     */
    @Transactional
    public void generateSeedData() {
        // 이미 충분한 데이터가 있으면 스킵
        long productCount = productRepository.count();
        if (productCount >= 200) {
            log.info("시드 데이터가 이미 존재합니다 (상품 {}개). 생성을 스킵합니다.", productCount);
            return;
        }

        log.info("========================================");
        log.info("JSON 파일 기반 시드 데이터 생성 시작");
        log.info("========================================");

        try {
            // JSON 파일 읽기
            ClassPathResource resource = new ClassPathResource("seed/naver_products.json");
            SeedData seedData = objectMapper.readValue(resource.getInputStream(), SeedData.class);

            log.info("JSON 파일 로드 완료: users={}, categories={}, sellers={}, products={}",
                    seedData.getUsers().size(),
                    seedData.getCategories().size(),
                    seedData.getSellers().size(),
                    seedData.getProducts().size());

            // 1. Users 생성
            List<User> users = createUsers(seedData.getUsers());
            log.info("✅ 사용자 {}개 생성 완료", users.size());

            // 2. Categories 생성
            List<Category> categories = createCategories(seedData.getCategories());
            log.info("✅ 카테고리 {}개 생성 완료", categories.size());

            // 3. Sellers 생성
            List<Seller> sellers = createSellers(seedData.getSellers(), users);
            log.info("✅ 판매자 {}개 생성 완료", sellers.size());

            // 4. Products 생성
            int productsCreated = createProducts(seedData.getProducts(), sellers, categories);
            log.info("✅ 상품 {}개 생성 완료", productsCreated);

            log.info("========================================");
            log.info("시드 데이터 생성 완료!");
            log.info("사용자: {}개, 카테고리: {}개, 판매자: {}개, 상품: {}개",
                    users.size(), categories.size(), sellers.size(), productsCreated);
            log.info("========================================");

        } catch (IOException e) {
            log.error("JSON 파일 읽기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("시드 데이터 JSON 파일을 읽을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("시드 데이터 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("시드 데이터 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Users 생성
     */
    private List<User> createUsers(List<UserDto> userDtos) {
        List<User> users = new ArrayList<>();

        for (UserDto dto : userDtos) {
            // 이미 존재하는 이메일이면 스킵
            if (userRepository.existsByEmail(dto.getEmail())) {
                userRepository.findByEmail(dto.getEmail()).ifPresent(users::add);
                continue;
            }

            User user = User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .name(dto.getName())
                    .phone(dto.getPhone())
                    .birthDate(LocalDate.parse(dto.getBirthDate()))
                    .isActive(dto.getIsActive())
                    .build();
                    .build();
            user.addRole(Role.valueOf(dto.getRole()));

            users.add(userRepository.save(user));
        }

        return users;
    }

    /**
     * Categories 생성
     */
    private List<Category> createCategories(List<CategoryDto> categoryDtos) {
        List<Category> categories = new ArrayList<>();

        for (CategoryDto dto : categoryDtos) {
            // 이미 존재하는 카테고리면 스킵
            List<Category> existing = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals(dto.getName()))
                    .toList();

            if (!existing.isEmpty()) {
                categories.add(existing.get(0));
                continue;
            }

            Category category = Category.builder()
                    .name(dto.getName())
                    .build();

            categories.add(categoryRepository.save(category));
        }

        return categories;
    }

    /**
     * Sellers 생성
     */
    private List<Seller> createSellers(List<SellerDto> sellerDtos, List<User> users) {
        List<Seller> sellers = new ArrayList<>();

        for (SellerDto dto : sellerDtos) {
            User user = users.get(dto.getUserIndex());

            // 이미 존재하는 판매자면 스킵
            if (sellerRepository.findByUserId(user.getId()).isPresent()) {
                sellerRepository.findByUserId(user.getId()).ifPresent(sellers::add);
                continue;
            }

            Seller seller = Seller.builder()
                    .user(user)
                    .businessName(dto.getBusinessName())
                    .businessNumber(dto.getBusinessNumber())
                    .minStockThreshold(dto.getMinStockThreshold())
                    .build();

            sellers.add(sellerRepository.save(seller));
        }

        return sellers;
    }

    /**
     * Products 생성
     */
    private int createProducts(List<ProductDto> productDtos, List<Seller> sellers, List<Category> categories) {
        int created = 0;

        for (ProductDto dto : productDtos) {
            try {
                Seller seller = sellers.get(dto.getSellerIndex());
                Category category = categories.get(dto.getCategoryIndex());

                Product product = Product.builder()
                        .seller(seller)
                        .category(category)
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .price(BigDecimal.valueOf(dto.getPrice()))
                        .stock(dto.getStock())
                        .imageUrl(dto.getImageUrl())
                        .naverProductId(dto.getNaverProductId())
                        .status(ProductStatus.valueOf(dto.getStatus()))
                        .salesCount(dto.getSalesCount())
                        .build();

                productRepository.save(product);
                created++;

            } catch (Exception e) {
                log.warn("상품 생성 실패: {}", e.getMessage());
            }
        }

        return created;
    }

    // ========== DTO 클래스들 ==========

    @Data
    public static class SeedData {
        private List<UserDto> users;
        private List<CategoryDto> categories;
        private List<SellerDto> sellers;
        private List<ProductDto> products;
        private MetadataDto metadata;
    }

    @Data
    public static class UserDto {
        private String email;
        private String password;
        private String name;
        private String phone;
        private String birthDate;
        private String role;
        private Boolean isActive;
    }

    @Data
    public static class CategoryDto {
        private String name;
    }

    @Data
    public static class SellerDto {
        private Integer userIndex;
        private String businessName;
        private String businessNumber;
        private Integer minStockThreshold;
    }

    @Data
    public static class ProductDto {
        private Integer sellerIndex;
        private Integer categoryIndex;
        private String name;
        private String description;
        private Integer price;
        private Integer stock;
        private String imageUrl;
        private String naverProductId;
        private String status;
        private Integer salesCount;
    }

    @Data
    public static class MetadataDto {
        private String generatedAt;
        private String source;
        private String description;
    }
}