# API 명세서

**E-Commerce Platform API Documentation**

Base URL: `http://113.198.66.68:10254`
Swagger UI: `http://113.198.66.68:10254/swagger-ui/index.html`

---

## API 개요

### 총 엔드포인트: **47개**

| 도메인 | 개수 | 인증 필요 | 권한 |
|--------|------|-----------|------|
| Auth | 4 | ❌ | - |
| Users | 4 | ✅ | USER, ADMIN |
| Sellers | 5 | ✅ | USER, SELLER, ADMIN |
| Categories | 4 | Mixed | ADMIN |
| Products | 8 | Mixed | SELLER, ADMIN |
| Orders | 5 | ✅ | USER |
| Reviews | 4 | ✅ | USER |
| Restock Votes | 4 | ✅ | USER |
| Restock Notifications | 4 | ✅ | USER, SELLER |
| Coupons | 4 | ✅ | ADMIN |
| User Coupons | 2 | ✅ | USER |
| Notifications | 4 | ✅ | USER, ADMIN |
| Health | 1 | ❌ | - |

---

## 🔐 인증

모든 인증이 필요한 API는 다음 헤더를 포함해야 합니다:

```http
Authorization: Bearer {access_token}
```

---

## 1. Auth API (4개)

### 1.1 회원가입
```http
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01"
}
```

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600000
}
```

**Errors:**
- `400` - 입력값 검증 실패
- `409` - 이메일 중복

---

### 1.2 로그인
```http
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600000
}
```

**Errors:**
- `401` - 인증 실패 (이메일/비밀번호 불일치)

---

### 1.3 Google 소셜 로그인
```http
GET /api/auth/google?code={authorization_code}
```

**Response:** `200 OK` (로그인 성공 시 JWT 토큰 반환)

---

### 1.4 Firebase 소셜 로그인
```http
GET /api/auth/firebase?idToken={firebase_id_token}
```

**Response:** `200 OK` (로그인 성공 시 JWT 토큰 반환)

---

## 2. Users API (4개)

### 2.1 내 정보 조회
```http
GET /api/users/me
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birthDate": "1990-01-01",
  "role": "ROLE_USER",
  "isActive": true,
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

### 2.2 내 정보 수정
```http
PUT /api/users/me
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "홍길동",
  "phone": "010-9999-8888",
  "birthDate": "1990-01-01"
}
```

**Response:** `200 OK`

---

### 2.3 회원 탈퇴
```http
DELETE /api/users/me
Authorization: Bearer {token}
```

**Response:** `204 No Content`

---

### 2.4 회원 목록 조회 (관리자)
```http
GET /api/users?page=0&size=20&sort=createdAt,DESC
Authorization: Bearer {admin_token}
```

**Response:** `200 OK`
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 153,
  "totalPages": 8
}
```

**Required Role:** `ROLE_ADMIN`

---

## 3. Sellers API (5개)

### 3.1 판매자 등록
```http
POST /api/sellers
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "businessNumber": "123-45-67890",
  "businessName": "홍길동 상점",
  "ceoName": "홍길동",
  "address": "서울시 강남구...",
  "phoneNumber": "02-1234-5678",
  "minStockThreshold": 10
}
```

**Response:** `201 Created`

---

### 3.2 내 판매자 정보 조회
```http
GET /api/sellers/me
Authorization: Bearer {seller_token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_SELLER`

---

### 3.3 내 판매자 정보 수정
```http
PUT /api/sellers/me
Authorization: Bearer {seller_token}
```

**Required Role:** `ROLE_SELLER`

---

### 3.4 판매자 등록 해제
```http
DELETE /api/sellers/me
Authorization: Bearer {seller_token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_SELLER`

---

### 3.5 판매자 대시보드
```http
GET /api/sellers/me/dashboard
Authorization: Bearer {seller_token}
```

**Response:** `200 OK`
```json
{
  "todayOrderCount": 15,
  "todayRevenue": 1500000,
  "reviewStats": {
    "averageRating": 4.5,
    "totalReviews": 120
  },
  "topSellingProducts": [
    {
      "productId": 1,
      "productName": "노트북",
      "salesCount": 50,
      "totalRevenue": 50000000
    }
  ],
  "lowStockProducts": [
    {
      "id": 2,
      "name": "마우스",
      "stock": 5
    }
  ],
  "salesChart": [
    {
      "date": "2025-12-01",
      "orderCount": 10,
      "revenue": 1000000
    }
  ]
}
```

**Required Role:** `ROLE_SELLER`

---

## 4. Categories API (4개)

### 4.1 카테고리 생성
```http
POST /api/categories
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "name": "전자제품",
  "parentId": null
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_ADMIN`

---

### 4.2 카테고리 목록 조회
```http
GET /api/categories
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "전자제품",
    "parentId": null,
    "children": [
      {
        "id": 2,
        "name": "노트북",
        "parentId": 1
      }
    ]
  }
]
```

**Auth Required:** ❌

---

### 4.3 카테고리 수정
```http
PUT /api/categories/{id}
Authorization: Bearer {admin_token}
```

**Required Role:** `ROLE_ADMIN`

---

### 4.4 카테고리 삭제
```http
DELETE /api/categories/{id}
Authorization: Bearer {admin_token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_ADMIN`

---

## 5. Products API (8개)

### 5.1 상품 등록
```http
POST /api/products
Authorization: Bearer {seller_token}
```

**Request Body:**
```json
{
  "name": "노트북",
  "description": "고성능 노트북",
  "price": 1500000,
  "stock": 100,
  "categoryId": 2,
  "imageUrl": "https://example.com/image.jpg",
  "naverProductId": "12345"
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_SELLER`

---

### 5.2 상품 목록 조회 (검색/필터/정렬)
```http
GET /api/products?keyword=노트북&categoryId=2&minPrice=1000000&maxPrice=2000000&page=0&size=12&sort=price,ASC
```

**Query Parameters:**
- `keyword`: 상품명 검색
- `categoryId`: 카테고리 ID
- `minPrice`, `maxPrice`: 가격 범위
- `status`: 상품 상태 (ACTIVE, OUT_OF_STOCK)
- `page`, `size`: 페이지네이션
- `sort`: 정렬 (createdAt,DESC | price,ASC | name,ASC)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "노트북",
      "price": 1500000,
      "stock": 100,
      "status": "ACTIVE",
      "averageRating": 4.5,
      "reviewCount": 20
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 153,
  "totalPages": 13
}
```

**Auth Required:** ❌

---

### 5.3 상품 상세 조회
```http
GET /api/products/{id}
```

**Response:** `200 OK`

**Auth Required:** ❌

---

### 5.4 상품 수정
```http
PUT /api/products/{id}
Authorization: Bearer {seller_token}
```

**Required Role:** `ROLE_SELLER`

---

### 5.5 상품 삭제
```http
DELETE /api/products/{id}
Authorization: Bearer {seller_token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_SELLER`

---

### 5.6 네이버 쇼핑 API 검색
```http
GET /api/products/naver/search?query=노트북
Authorization: Bearer {seller_token}
```

**Response:** `200 OK`
```json
{
  "items": [
    {
      "title": "삼성 노트북",
      "link": "https://...",
      "image": "https://...",
      "lprice": "1500000",
      "hprice": "2000000",
      "productId": "12345"
    }
  ]
}
```

**Required Role:** `ROLE_SELLER`

---

### 5.7 재고 업데이트
```http
PUT /api/products/{id}/stock
Authorization: Bearer {seller_token}
```

**Request Body:**
```json
{
  "stock": 50
}
```

**Response:** `200 OK`

**Required Role:** `ROLE_SELLER`

---

### 5.8 상품 리뷰 목록
```http
GET /api/products/{id}/reviews?page=0&size=20&sort=createdAt,DESC
```

**Response:** `200 OK`

**Auth Required:** ❌

---

## 6. Orders API (5개)

### 6.1 주문 생성
```http
POST /api/orders
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "address": "서울시 강남구...",
  "couponId": 1
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_USER`

---

### 6.2 주문 목록 조회
```http
GET /api/orders?page=0&size=10&sort=createdAt,DESC
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 6.3 주문 상세 조회
```http
GET /api/orders/{id}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "userName": "홍길동",
  "items": [
    {
      "productId": 1,
      "productName": "노트북",
      "price": 1500000,
      "quantity": 2
    }
  ],
  "totalPrice": 3000000,
  "address": "서울시 강남구...",
  "status": "PENDING",
  "createdAt": "2025-12-26T10:00:00Z"
}
```

**Required Role:** `ROLE_USER`

---

### 6.4 주문 정보 수정
```http
PUT /api/orders/{id}
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "address": "서울시 서초구...",
  "status": "CONFIRMED"
}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 6.5 주문 취소
```http
DELETE /api/orders/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_USER`

---

## 7. Reviews API (4개)

### 7.1 리뷰 작성
```http
POST /api/reviews
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "productId": 1,
  "rating": 5,
  "content": "정말 좋은 상품입니다!"
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_USER`

---

### 7.2 내 리뷰 목록
```http
GET /api/reviews/my?page=0&size=20&sort=createdAt,DESC
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 7.3 리뷰 수정
```http
PUT /api/reviews/{id}
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "rating": 4,
  "content": "수정된 리뷰입니다."
}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 7.4 리뷰 삭제
```http
DELETE /api/reviews/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_USER`

---

## 8. Restock Votes API (4개)

### 8.1 재입고 투표
```http
POST /api/restock-votes
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "productId": 1
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_USER`

---

### 8.2 내 투표 목록
```http
GET /api/restock-votes/my?page=0&size=20
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 8.3 상품별 투표 목록
```http
GET /api/products/{productId}/restock-votes?page=0&size=20
```

**Response:** `200 OK`

**Auth Required:** ❌

---

### 8.4 투표 취소
```http
DELETE /api/restock-votes/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_USER`

---

## 9. Restock Notifications API (4개)

### 9.1 재입고 알림 신청
```http
POST /api/restock-notifications
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "productId": 1
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_USER`

---

### 9.2 내 알림 신청 목록
```http
GET /api/restock-notifications/my?page=0&size=20
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 9.3 상품별 알림 신청 목록
```http
GET /api/products/{productId}/restock-notifications?page=0&size=20
Authorization: Bearer {seller_token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_SELLER`

---

### 9.4 알림 신청 취소
```http
DELETE /api/restock-notifications/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_USER`

---

## 10. Coupons API (4개)

### 10.1 쿠폰 생성
```http
POST /api/coupons
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "name": "신규가입 쿠폰",
  "type": "WELCOME",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "minOrderAmount": 10000,
  "maxUsageCount": 1000,
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_ADMIN`

---

### 10.2 쿠폰 목록
```http
GET /api/coupons?page=0&size=20
Authorization: Bearer {admin_token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_ADMIN`

---

### 10.3 쿠폰 수정
```http
PUT /api/coupons/{id}
Authorization: Bearer {admin_token}
```

**Required Role:** `ROLE_ADMIN`

---

### 10.4 쿠폰 삭제
```http
DELETE /api/coupons/{id}
Authorization: Bearer {admin_token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_ADMIN`

---

## 11. User Coupons API (2개)

### 11.1 내 쿠폰함
```http
GET /api/user-coupons/my?availableOnly=true&page=0&size=20
Authorization: Bearer {token}
```

**Query Parameters:**
- `availableOnly`: `true` - 사용 가능한 쿠폰만 조회

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "신규가입 쿠폰",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "minOrderAmount": 10000,
      "isUsed": false,
      "expiresAt": "2025-12-31T23:59:59Z"
    }
  ],
  "page": 0,
  "size": 20
}
```

**Required Role:** `ROLE_USER`

---

### 11.2 쿠폰 사용
```http
POST /api/user-coupons/{id}/use
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

## 12. Notifications API (4개)

### 12.1 알림 생성 (관리자)
```http
POST /api/notifications
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "userId": 1,
  "type": "ORDER",
  "title": "주문 완료",
  "content": "주문이 완료되었습니다."
}
```

**Response:** `201 Created`

**Required Role:** `ROLE_ADMIN`

---

### 12.2 알림 목록
```http
GET /api/notifications?page=0&size=20&sort=createdAt,DESC
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 12.3 알림 읽음 처리
```http
PUT /api/notifications/{id}
Authorization: Bearer {token}
```

**Response:** `200 OK`

**Required Role:** `ROLE_USER`

---

### 12.4 알림 삭제
```http
DELETE /api/notifications/{id}
Authorization: Bearer {token}
```

**Response:** `204 No Content`

**Required Role:** `ROLE_USER`

---

## 13. Health API (1개)

### 13.1 헬스체크
```http
GET /health
```

**Response:** `200 OK`
```json
{
  "status": "UP",
  "application": "E-Commerce Platform",
  "version": "1.0.0",
  "buildTime": "2025-12-26T00:00:00Z",
  "timestamp": "2025-12-26T10:00:00Z"
}
```

**Auth Required:** ❌

---

## 📜 에러 코드

| HTTP | Code | Message |
|------|------|---------|
| 400 | `BAD_REQUEST` | 잘못된 요청입니다 |
| 400 | `VALIDATION_FAILED` | 입력값 검증 실패 |
| 400 | `INVALID_QUERY_PARAM` | 쿼리 파라미터가 잘못되었습니다 |
| 401 | `UNAUTHORIZED` | 인증이 필요합니다 |
| 401 | `TOKEN_EXPIRED` | 토큰이 만료되었습니다 |
| 401 | `INVALID_CREDENTIALS` | 이메일 또는 비밀번호가 올바르지 않습니다 |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다 |
| 404 | `RESOURCE_NOT_FOUND` | 리소스를 찾을 수 없습니다 |
| 404 | `USER_NOT_FOUND` | 사용자를 찾을 수 없습니다 |
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없습니다 |
| 409 | `DUPLICATE_RESOURCE` | 중복된 리소스입니다 |
| 409 | `DUPLICATE_EMAIL` | 이미 사용 중인 이메일입니다 |
| 409 | `STATE_CONFLICT` | 리소스 상태 충돌 |
| 422 | `UNPROCESSABLE_ENTITY` | 처리할 수 없는 요청입니다 |
| 429 | `TOO_MANY_REQUESTS` | 요청 한도 초과 |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다 |
| 500 | `DATABASE_ERROR` | 데이터베이스 오류 |
| 503 | `SERVICE_UNAVAILABLE` | 서비스를 일시적으로 사용할 수 없습니다 |

---

**Last Updated**: 2025-12-26