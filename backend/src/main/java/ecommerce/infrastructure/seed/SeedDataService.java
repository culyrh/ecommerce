package ecommerce.infrastructure.seed;

import ecommerce.common.enums.Role;
import ecommerce.domain.category.entity.Category;
import ecommerce.domain.category.repository.CategoryRepository;
import ecommerce.domain.product.dto.NaverProductSearchResponse;
import ecommerce.domain.product.entity.Product;
import ecommerce.domain.product.enums.ProductStatus;
import ecommerce.domain.product.repository.ProductRepository;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import ecommerce.infrastructure.naver.NaverShoppingApiClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 시드 데이터 생성 서비스
 * 네이버 쇼핑 API를 활용하여 200개 이상의 상품 데이터 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedDataService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final NaverShoppingApiClient naverApiClient;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @PostConstruct
    @Transactional
    public void generateSeedData() {
        // 이미 충분한 데이터가 있으면 스킵
        long productCount = productRepository.count();
        if (productCount >= 200) {
            log.info("시드 데이터가 이미 존재합니다 (상품 {}개). 생성을 스킵합니다.", productCount);
            return;
        }

        log.info("========================================");
        log.info("시드 데이터 생성 시작");
        log.info("========================================");

        try {
            // 1. 카테고리 생성 (10개)
            List<Category> categories = createCategories();
            log.info("✅ 카테고리 {}개 생성 완료", categories.size());

            // 2. 판매자 생성 (5개)
            List<Seller> sellers = createSellers();
            log.info("✅ 판매자 {}개 생성 완료", sellers.size());

            // 3. 네이버 API로 상품 생성 (200개 목표)
            int productsCreated = createProductsFromNaverApi(sellers, categories);
            log.info("✅ 상품 {}개 생성 완료", productsCreated);

            log.info("========================================");
            log.info("시드 데이터 생성 완료!");
            log.info("카테고리: {}개, 판매자: {}개, 상품: {}개",
                    categories.size(), sellers.size(), productsCreated);
            log.info("========================================");

        } catch (Exception e) {
            log.error("시드 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 카테고리 10개 생성
     */
    private List<Category> createCategories() {
        List<Category> categories = new ArrayList<>();
        String[] categoryNames = {
                "전자제품", "의류·패션", "식품·음료", "가구·인테리어", "도서·문구",
                "스포츠·레저", "완구·취미", "화장품·미용", "생활용품", "가전제품"
        };

        for (String name : categoryNames) {
            // 기존에 있는지 확인
            List<Category> existing = categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equals(name))
                    .toList();

            if (existing.isEmpty()) {
                Category category = Category.builder()
                        .name(name)
                        .build();
                categories.add(categoryRepository.save(category));
            } else {
                categories.add(existing.get(0));
            }
        }

        return categories;
    }

    /**
     * 판매자 5개 생성
     */
    private List<Seller> createSellers() {
        List<Seller> sellers = new ArrayList<>();
        String[] sellerNames = {
                "테크스토어", "패션하우스", "푸드마켓", "홈앤리빙", "북스토어"
        };

        for (int i = 0; i < sellerNames.length; i++) {
            String email = "seller" + (i + 1) + "@example.com";

            if (userRepository.findByEmail(email).isEmpty()) {
                // 사용자 생성
                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("password123"))
                        .name(sellerNames[i] + " 대표")
                        .phone("010-1234-" + String.format("%04d", 1000 + i))
                        .birthDate(LocalDate.of(1985, 1, 1).plusYears(i))
                        .role(Role.ROLE_SELLER)
                        .isActive(true)
                        .build();
                userRepository.save(user);

                // 판매자 생성
                Seller seller = Seller.builder()
                        .user(user)
                        .businessName(sellerNames[i])
                        .businessNumber("123-45-" + String.format("%05d", 10000 + i))
                        .minStockThreshold(10)
                        .build();
                sellers.add(sellerRepository.save(seller));
            } else {
                userRepository.findByEmail(email).ifPresent(user -> {
                    sellerRepository.findByUserId(user.getId()).ifPresent(sellers::add);
                });
            }
        }

        return sellers;
    }

    /**
     * 네이버 API로 상품 200개 생성
     */
    private int createProductsFromNaverApi(List<Seller> sellers, List<Category> categories) {
        String[] keywords = {"노트북", "스마트폰", "의류", "식품", "가구", "책", "운동화", "화장품"};
        int targetProducts = 200;
        int productsPerKeyword = targetProducts / keywords.length;
        int totalCreated = 0;

        for (String keyword : keywords) {
            try {
                log.info("키워드 '{}' 상품 검색 중...", keyword);
                NaverProductSearchResponse response = naverApiClient.searchProducts(keyword, productsPerKeyword, 1);

                if (response != null && response.getItems() != null) {
                    for (NaverProductSearchResponse.NaverProduct item : response.getItems()) {
                        try {
                            // 랜덤 판매자 및 카테고리 선택
                            Seller seller = sellers.get(random.nextInt(sellers.size()));
                            Category category = categories.get(random.nextInt(categories.size()));

                            // 가격 파싱 (HTML 태그 제거, 숫자만 추출)
                            String priceStr = item.getLprice().replaceAll("[^0-9]", "");
                            BigDecimal price = new BigDecimal(priceStr.isEmpty() ? "10000" : priceStr);

                            // 재고는 랜덤 (0~100)
                            int stock = random.nextInt(101);

                            // 상품명에서 HTML 태그 제거
                            String productName = item.getTitle().replaceAll("<[^>]*>", "");
                            if (productName.length() > 255) {
                                productName = productName.substring(0, 252) + "...";
                            }

                            // 상품 생성
                            Product product = Product.builder()
                                    .seller(seller)
                                    .category(category)
                                    .name(productName)
                                    .description(keyword + " 관련 상품입니다. " + productName)
                                    .price(price)
                                    .stock(stock)
                                    .status(stock > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK)
                                    .imageUrl(item.getImage() != null ? item.getImage() : "https://via.placeholder.com/300")
                                    .build();

                            productRepository.save(product);
                            totalCreated++;

                            if (totalCreated >= targetProducts) {
                                log.info("목표 상품 수({})에 도달했습니다.", targetProducts);
                                return totalCreated;
                            }

                        } catch (Exception e) {
                            log.warn("상품 생성 실패: {}", e.getMessage());
                        }
                    }
                }

            } catch (Exception e) {
                log.error("키워드 '{}' 검색 실패: {}", keyword, e.getMessage());
            }
        }

        // 목표에 미달하면 임의 상품 생성
        while (totalCreated < targetProducts) {
            try {
                Seller seller = sellers.get(random.nextInt(sellers.size()));
                Category category = categories.get(random.nextInt(categories.size()));

                Product product = Product.builder()
                        .seller(seller)
                        .category(category)
                        .name("상품 " + (totalCreated + 1))
                        .description("임의 생성 상품입니다.")
                        .price(BigDecimal.valueOf(random.nextInt(900000) + 10000))
                        .stock(random.nextInt(101))
                        .status(ProductStatus.ACTIVE)
                        .imageUrl("https://via.placeholder.com/300")
                        .build();

                productRepository.save(product);
                totalCreated++;

            } catch (Exception e) {
                log.error("임의 상품 생성 실패: {}", e.getMessage());
                break;
            }
        }

        return totalCreated;
    }
}