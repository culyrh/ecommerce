# 시스템 아키텍처

**E-Commerce Platform - Architecture Documentation**

---

## 시스템 개요

### 아키텍처 패턴
- **Layered Architecture** (계층형 아키텍처)
- **Domain-Driven Design (DDD)** 기반 패키지 구조
- **RESTful API** 설계

### 기술 스택

#### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.5
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT
- **API Documentation**: Swagger

#### Database
- **Primary DB**: PostgreSQL 15
- **Cache**: Redis 7
- **Connection Pool**: HikariCP

#### External Services
- **OAuth**: Naver OAuth2, Firebase Authentication
- **API Integration**: Naver Shopping API

#### Infrastructure
- **Container**: Docker, Docker Compose
- **Deployment**: JCloud (Ubuntu 22.04)
- **Process Management**: Docker Compose

---

## 🏗️ 시스템 구조도

```
┌──────────────────────────────────────────────────────────────┐
│                   API Gateway Layer                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Frontend Static Files Serving                │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────────┐
│                  Application Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Spring Boot Application (Port 8080)          │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  Security Layer (JWT Authentication)        │    │   │
│  │  │  - JwtAuthenticationFilter                  │    │   │
│  │  │  - Spring Security Config                   │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  Controller Layer (REST Endpoints)          │    │   │
│  │  │  - AuthController, UserController, etc.     │    │   │
│  │  │  - @RestController, @RequestMapping         │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  Service Layer (Business Logic)             │    │   │
│  │  │  - @Service, @Transactional                 │    │   │
│  │  │  - Domain Services                          │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  Repository Layer (Data Access)             │    │   │
│  │  │  - Spring Data JPA Repositories             │    │   │
│  │  │  - @Repository, JpaRepository               │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬─────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐  ┌──────▼──────┐  ┌────▼────────┐
│  PostgreSQL  │  │    Redis    │  │  External   │
│  (Port 5432) │  │ (Port 6379) │  │   APIs      │
│              │  │             │  │             │
│  12 Tables   │  │  - Cache    │  │  - Firebase │
│  - users     │  │  - Session  │  │  - Naver    │
│  - products  │  │  - Counter  │  │             │
│  - orders    │  │             │  │             │
│  - ...       │  │             │  │             │
└──────────────┘  └─────────────┘  └─────────────┘
```

---

## 레이어 아키텍처

### 1. Presentation Layer (Controller)
**책임**: HTTP 요청/응답 처리, DTO 변환, 인증/인가

```
ecommerce/domain/{domain}/controller/
├── AuthController.java        - 인증/인가 엔드포인트
├── UserController.java        - 회원 관리
├── ProductController.java     - 상품 관리
├── OrderController.java       - 주문 관리
└── ...
```

**주요 역할**:
- HTTP 요청 파라미터 검증 (`@Valid`)
- DTO → Entity 변환 (Service 호출)
- 응답 상태 코드 설정 (`ResponseEntity`)
- Swagger 문서화 (`@Operation`, `@ApiResponse`)

---

### 2. Service Layer (Business Logic)
**책임**: 비즈니스 로직, 트랜잭션 관리, 도메인 규칙

```
ecommerce/domain/{domain}/service/
├── AuthService.java           - 회원가입, 로그인, JWT 발급
├── UserService.java           - 회원 정보 관리
├── ProductService.java        - 상품 CRUD, 검색, 필터링
├── OrderService.java          - 주문 생성, 쿠폰 적용
└── ...
```

**주요 역할**:
- 비즈니스 규칙 검증
- 트랜잭션 경계 설정 (`@Transactional`)
- Repository 호출 및 Entity 조작
- 이벤트 발행 (`ApplicationEventPublisher`)

---

### 3. Repository Layer (Data Access)
**책임**: 데이터베이스 CRUD, 쿼리 실행

```
ecommerce/domain/{domain}/repository/
├── UserRepository.java        - Spring Data JPA Repository
├── ProductRepository.java     - 커스텀 쿼리 메서드
├── OrderRepository.java       - Specification 지원
└── ...
```

**주요 역할**:
- JPA 기본 CRUD (`save`, `findById`, `delete`)
- 커스텀 쿼리 메서드 (`findByEmail`, `findByCategory`)
- Specification 동적 쿼리 (`ProductSpecification`)
- 페이지네이션/정렬 지원 (`Pageable`)

---

### 4. Domain Layer (Entity, DTO)
**책임**: 도메인 모델, 데이터 전송 객체

```
ecommerce/domain/{domain}/
├── entity/                    - JPA 엔티티
│   ├── User.java
│   ├── Product.java
│   └── ...
├── dto/                       - 요청/응답 DTO
│   ├── LoginRequest.java
│   ├── ProductResponse.java
│   └── ...
└── enums/                     - 열거형
    ├── Role.java
    ├── OrderStatus.java
    └── ...
```

**주요 역할**:
- 데이터베이스 스키마 정의 (`@Entity`, `@Table`)
- 연관관계 매핑 (`@ManyToOne`, `@OneToMany`)
- Validation 규칙 (`@NotNull`, `@Email`, `@Size`)
- DTO 변환 (`toEntity()`, `fromEntity()`)

---

### 5. Common Layer (공통 기능)
**책임**: 전역 설정, 보안, 예외 처리

```
ecommerce/common/
├── config/                    - 설정 클래스
│   ├── SecurityConfig.java   - Spring Security 설정
│   ├── SwaggerConfig.java    - Swagger 설정
│   └── RedisConfig.java      - Redis 설정
├── security/                  - 인증/인가
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── exception/                 - 예외 처리
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ErrorCode.java
└── enums/                     - 공통 열거형
    └── Role.java
```

---

## 보안 아키텍처

### JWT 인증 플로우

```
1. 로그인 요청
   POST /api/auth/login
   { "email": "user@example.com", "password": "password" }
        ↓
2. AuthService 인증 처리
   - 이메일/비밀번호 검증
   - User 조회 (UserRepository)
   - BCrypt 비밀번호 확인
        ↓
3. JwtTokenProvider 토큰 발급
   - Access Token (1시간)
   - Refresh Token (7일)
        ↓
4. 응답
   { "accessToken": "eyJ...", "refreshToken": "eyJ..." }

---

5. 인증이 필요한 API 호출
   GET /api/users/me
   Authorization: Bearer eyJ...
        ↓
6. JwtAuthenticationFilter 검증
   - Bearer 토큰 추출
   - JwtTokenProvider.validateToken()
   - 토큰에서 email, role 추출
        ↓
7. SecurityContext에 인증 정보 저장
   UsernamePasswordAuthenticationToken
        ↓
8. Controller 실행
   @PreAuthorize("hasRole('USER')")
```

---

## 💾 데이터베이스 아키텍처

### ERD (Entity Relationship Diagram)

**12개 테이블 구조**:

```
users (회원)
  ├─ id (PK)
  ├─ email (UNIQUE)
  ├─ password (BCrypt)
  ├─ role (ROLE_USER, ROLE_SELLER, ROLE_ADMIN)
  └─ ...

sellers (판매자)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ business_number
  └─ ...

categories (카테고리)
  ├─ id (PK)
  ├─ parent_id (FK → categories, Self-Join)
  └─ name

products (상품)
  ├─ id (PK)
  ├─ seller_id (FK → sellers)
  ├─ category_id (FK → categories)
  ├─ name, price, stock
  └─ ...

orders (주문)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ status (PENDING, PAID, SHIPPED, ...)
  └─ ...

order_items (주문 항목)
  ├─ id (PK)
  ├─ order_id (FK → orders)
  ├─ product_id (FK → products)
  └─ quantity, price

reviews (리뷰)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ product_id (FK → products)
  └─ rating, content

coupons (쿠폰)
  ├─ id (PK)
  ├─ type (WELCOME, BIRTHDAY, VIP)
  └─ discount_type, discount_value

user_coupons (사용자 쿠폰)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ coupon_id (FK → coupons)
  └─ is_used, used_at

restock_votes (재입고 투표)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ product_id (FK → products)
  └─ ...

restock_notifications (재입고 알림)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ product_id (FK → products)
  └─ ...

notifications (알림)
  ├─ id (PK)
  ├─ user_id (FK → users)
  ├─ type (ORDER, RESTOCK, COUPON)
  └─ title, content
```

### 인덱스 전략

**검색 성능 최적화를 위한 인덱스**:

```sql
-- users 테이블
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- products 테이블
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_sales_count ON products(sales_count);

-- orders 테이블
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- reviews 테이블
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
```

### N+1 문제 해결

**Fetch Join 사용**:

```java
// ProductService.java
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.category " +
       "LEFT JOIN FETCH p.seller " +
       "WHERE p.id = :id")
Optional<Product> findByIdWithDetails(@Param("id") Long id);
```

---

## 배포 아키텍처

### Docker Compose 구성

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: ecommerce-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - ecommerce-network

  redis:
    image: redis:7-alpine
    container_name: ecommerce-redis
    ports:
      - "6379:6379"
    networks:
      - ecommerce-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ecommerce-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATA_REDIS_HOST: redis
    ports:
      - "10254:8080"
    depends_on:
      - postgres
      - redis
    networks:
      - ecommerce-network

  frontend:
    image: nginx:alpine
    container_name: ecommerce-frontend
    volumes:
      - ./frontend/build:/usr/share/nginx/html:ro
    ports:
      - "13254:80"
    depends_on:
      - backend
    networks:
      - ecommerce-network

volumes:
  postgres_data:

networks:
  ecommerce-network:
    driver: bridge
```

### JCloud 배포 구조

```
JCloud Instance (Ubuntu 22.04)
  IP: 113.198.66.68
  
  ├─ Docker Network: ecommerce-network
  │
  ├─ Container: ecommerce-postgres
  │  └─ Port: 5432
  │
  ├─ Container: ecommerce-redis
  │  └─ Port: 6379
  │
  ├─ Container: ecommerce-backend
  │  ├─ Internal Port: 8080
  │  └─ External Port: 10254 (Port Forwarding)
  │
  └─ Container: ecommerce-frontend
     ├─ Internal Port: 80
     └─ External Port: 13254 (Port Forwarding)
```

---

## 성능 최적화

### Redis 캐싱 전략

**1. 판매 순위 캐싱** (10분마다 갱신)
```java
@Scheduled(cron = "0 */10 * * * *")
public void updateSalesRanking() {
    List<Product> topProducts = productRepository
        .findTop10ByOrderBySalesCountDesc();
    
    redisService.setValue(
        "sales:ranking:top10",
        topProducts,
        Duration.ofMinutes(10)
    );
}
```

**2. 재입고 투표 카운팅**
```java
public void voteForRestock(Long productId) {
    String key = "restock:votes:" + productId;
    redisService.increment(key);
}
```

**3. 대시보드 통계 캐싱**
```java
public DashboardResponse getDashboard(String email) {
    String cacheKey = "dashboard:" + email;
    DashboardResponse cached = redisService.getValue(cacheKey);
    
    if (cached == null) {
        cached = calculateDashboard(email);
        redisService.setValue(cacheKey, cached, Duration.ofHours(1));
    }
    
    return cached;
}
```

### Connection Pool 설정

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 이벤트 기반 아키텍처

### 재입고 알림 이벤트

**Event 발행**:
```java
// ProductService.java
public void updateStock(Long productId, Integer newStock) {
    Product product = findById(productId);
    Integer oldStock = product.getStock();
    product.setStock(newStock);
    
    // 품절 → 재입고 시 이벤트 발행
    if (oldStock == 0 && newStock > 0) {
        eventPublisher.publishEvent(
            new ProductRestockedEvent(productId, newStock)
        );
    }
}
```

**Event 리스너**:
```java
// RestockEventListener.java
@EventListener
@Async
public void handleProductRestocked(ProductRestockedEvent event) {
    // 1. 재입고 투표자들에게 알림
    List<RestockVote> votes = restockVoteRepository
        .findByProductId(event.getProductId());
    
    for (RestockVote vote : votes) {
        notificationService.sendRestockNotification(
            vote.getUser(),
            event.getProductId()
        );
    }
    
    // 2. 재입고 알림 신청자들에게 알림
    List<RestockNotification> notifications = 
        restockNotificationRepository
            .findByProductId(event.getProductId());
    
    for (RestockNotification noti : notifications) {
        notificationService.sendRestockNotification(
            noti.getUser(),
            event.getProductId()
        );
    }
}
```

---

## 스케줄러 아키텍처

### 1. 자동 발주 권장 (매일 00:00)
```java
@Scheduled(cron = "0 0 0 * * *")
public void recommendReorder() {
    List<Product> lowStockProducts = productRepository
        .findByStockLessThanMinThreshold();
    
    for (Product product : lowStockProducts) {
        int avgSales = calculateAverageSales(product, 7);
        int recommendedQty = avgSales * 7 - product.getStock();
        
        notificationService.sendReorderRecommendation(
            product.getSeller(),
            product,
            recommendedQty
        );
    }
}
```

### 2. 생일 쿠폰 발급 (매일 01:00)
```java
@Scheduled(cron = "0 0 1 * * *")
public void issueBirthdayCoupons() {
    LocalDate today = LocalDate.now();
    List<User> birthdayUsers = userRepository
        .findByBirthDateMonthAndDay(
            today.getMonthValue(),
            today.getDayOfMonth()
        );
    
    Coupon birthdayCoupon = couponRepository
        .findByType(CouponType.BIRTHDAY);
    
    for (User user : birthdayUsers) {
        userCouponService.issueCoupon(user, birthdayCoupon);
    }
}
```

---

## 확장 가능성

### 향후 개선 방안

1. **마이크로서비스 분리**
    - User Service, Product Service, Order Service 독립 배포

2. **Database Replication**
    - Master-Slave 구조
    - Read/Write 분리

3. **메시지 큐 도입**
    - RabbitMQ, Kafka
    - 비동기 알림 발송

4. **CDN 연동**
    - 이미지 파일 S3 + CloudFront
    - 정적 파일 캐싱

5. **Kubernetes 배포**
    - Auto Scaling
    - Rolling Update
    - Health Check & Self-Healing

---