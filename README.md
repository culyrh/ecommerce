# 🛒 E-Commerce Platform

입점 중개형 전자상거래 플랫폼 - Term Project

## 프로젝트 개요

### 주제
**입점 중개형 전자상거래 플랫폼**

여러 판매자가 입점하여 상품을 판매하고, 고객은 다양한 상품을 구매할 수 있는 B2B2C 형태의 전자상거래 플랫폼입니다.

### 주요 기능
- **인증/인가**: JWT + Firebase Auth + Google OAuth2
- **3-Tier 사용자 관리**: USER, SELLER, ADMIN
- **상품 관리**: 카테고리, 상품 CRUD, 재고 관리, 네이버 쇼핑 API 연동
- **주문 관리**: 장바구니, 주문, 결제, 배송 추적
- **리뷰 시스템**: 상품 리뷰 작성/수정/삭제
- **재입고 관리**: 재입고 투표, 재입고 알림 (Redis + Event)
- **쿠폰 시스템**: 스마트 쿠폰 (웰컴/생일/VIP 자동 발급)
- **알림 시스템**: 주문/재입고/쿠폰 알림
- **판매자 대시보드**: 매출 통계, 베스트셀러, 재고 부족 상품
- **자동 발주 권장**: AI 기반 재고 예측 (스케줄러)

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle 8.5
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### Database
- **Main DB**: PostgreSQL 15
- **Cache/Session**: Redis 7

### API
- **OAuth**: Naver OAuth2, Firebase Auth
- **Shopping**: Naver Shopping API (json으로 추출)

### 환경
- **Container**: Docker, Docker Compose
- **Deployment**: JCloud
- **CI/CD**: GitHub Actions

---

## 빠른 시작

### 1. 사전 요구사항
- Docker & Docker Compose
- JDK 17 이상 (로컬 개발 시)

### 2. 환경 변수 설정

`.env` 파일 생성 (`.env.example` 참고):

```bash
# Database
POSTGRES_DB=ecommerce
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret-key-here-min-32-characters-required-for-hs256
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY=your-firebase-private-key
FIREBASE_CLIENT_EMAIL=your-firebase-client-email

# Naver Shopping API
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Backend
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Frontend
REACT_APP_BACKEND_URL=http://localhost:8080
```

### 3. Docker Compose로 실행

```bash
# 모든 서비스 시작 (PostgreSQL, Redis, Backend, Frontend)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down
```

---

## 🌐 접속 정보

### 배포 서버 (JCloud)
- **Base URL**: http://113.198.66.68:10254
- **Swagger UI**: http://113.198.66.68:10254/swagger-ui/index.html
- **Health Check**: http://113.198.66.68:10254/health
- **Frontend**: http://113.198.66.68:13254

---

## 테스트 계정

### 일반 사용자 (ROLE_USER)
- Email: `user@example.com`
- Password: `a123456@`

### 판매자 (ROLE_SELLER)
- Email: `seller@example.com`
- Password: `a123456@`

### 관리자 (ROLE_ADMIN)
- Email: `admin@example.com`
- Password: `admin1234`

---

## API 명세

### API 개수: **총 47개**

| 도메인 | 엔드포인트 수 | 주요 기능 |
|--------|--------------|----------|
| Auth | 4 | 회원가입, 로그인, Google/Firebase OAuth |
| Users | 4 | 내 정보 조회/수정/삭제, 회원 목록(Admin) |
| Sellers | 5 | 판매자 등록/조회/수정/삭제, 대시보드 |
| Categories | 4 | 카테고리 CRUD |
| Products | 8 | 상품 CRUD, 검색/정렬/페이징, Naver API, 재고 관리 |
| Orders | 5 | 주문 CRUD, 주문 목록 |
| Reviews | 4 | 리뷰 CRUD |
| Restock Votes | 4 | 재입고 투표 CRUD |
| Restock Notifications | 4 | 재입고 알림 CRUD |
| Coupons | 4 | 쿠폰 CRUD (Admin) |
| User Coupons | 2 | 내 쿠폰 조회, 쿠폰 사용 |
| Notifications | 4 | 알림 CRUD |
| Health | 1 | 헬스체크 |

**상세 API 명세**: [docs/API_명세서.md](docs/API_명세서.md)

---

## 인증/인가

### JWT 토큰 방식
1. 로그인 → Access Token + Refresh Token 발급
2. API 요청 시 `Authorization: Bearer {access_token}` 헤더 포함
3. Access Token 만료 시 Refresh Token으로 재발급

### 소셜 로그인
- **Naver OAuth2**

### 역할 기반 접근 제어 (RBAC)

| 역할 | 권한 |
|------|------|
| **ROLE_USER** | 주문, 리뷰, 재입고 투표/알림, 쿠폰 사용 |
| **ROLE_SELLER** | USER 권한 + 상품 관리, 재고 관리, 판매 대시보드 |
| **ROLE_ADMIN** | SELLER 권한 + 회원 관리, 카테고리 관리, 쿠폰 관리 |

---

## 핵심 기능

### 1. 재입고 관리 시스템
- **재입고 투표**: 사용자가 품절 상품에 투표
- **Redis 카운팅**: 실시간 투표 수 집계
- **자동 알림**: 재입고 시 투표/알림 신청 사용자에게 알림 발송
- **Event-Driven**: Spring Events로 비동기 처리

### 2. 스마트 쿠폰 시스템
- **웰컴 쿠폰**: 회원가입 시 자동 발급
- **생일 쿠폰**: 매일 자동 스케줄러로 생일자에게 발급
- **VIP 쿠폰**: 누적 구매금액 기준 자동 발급
- **쿠폰 검증**: 최소 주문금액, 만료일, 사용 여부 체크

### 3. 판매자 대시보드
- **오늘의 통계**: 주문 수, 매출, 평균 평점
- **베스트셀러**: 판매량 Top 5 (Redis 캐싱)
- **재고 부족 상품**: 임계값 이하 상품 알림
- **최근 30일 매출 차트**: 일별 매출 그래프

### 4. 자동 발주 권장 (스케줄러)
- **매일 00:00 실행**: 재고 부족 상품 감지
- **평균 판매량 기반**: 지난 7일 평균 × 안전 계수
- **발주 권장 수량 계산**: 목표 재고 - 현재 재고

---

## 에러 처리

### 표준 에러 응답 형식
```json
{
  "timestamp": "2025-12-26T10:00:00Z",
  "path": "/api/products/999",
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다",
  "details": { "productId": 999 }
}
```

### HTTP 상태 코드 (12종 이상)

| 코드 | 설명 |
|------|------|
| 200 | OK - 성공 |
| 201 | Created - 생성 성공 |
| 204 | No Content - 삭제 성공 |
| 400 | Bad Request - 잘못된 요청 |
| 401 | Unauthorized - 인증 실패 |
| 403 | Forbidden - 권한 없음 |
| 404 | Not Found - 리소스 없음 |
| 409 | Conflict - 중복 리소스 |
| 422 | Unprocessable Entity - 처리 불가 |
| 429 | Too Many Requests - 요청 한도 초과 |
| 500 | Internal Server Error - 서버 오류 |
| 503 | Service Unavailable - 서비스 불가 |

---

## 검색/필터링/정렬

### 상품 검색 예시
```
GET /api/products?keyword=노트북&minPrice=500000&maxPrice=1500000&category=1&page=0&size=12&sort=price,ASC
```

**파라미터:**
- `keyword`: 상품명 검색
- `minPrice`, `maxPrice`: 가격 범위
- `categoryId`: 카테고리 ID
- `page`, `size`: 페이지네이션
- `sort`: 정렬 (createdAt,DESC | price,ASC | name,ASC)

**응답:**
```json
{
  "content": [...],
  "page": 0,
  "size": 12,
  "totalElements": 153,
  "totalPages": 13,
  "sort": "price,ASC"
}
```

---

## 보안

### 구현된 보안 기능
- JWT 토큰 인증
- 비밀번호 BCrypt 해싱
- CORS 설정
- SQL Injection 방지 (JPA PreparedStatement)
- XSS 방지 (입력 검증)
- 환경변수로 민감 정보 관리
- HTTPS 권장 (프로덕션)

---

## 성능 최적화

- **Redis 캐싱**: 베스트셀러, 대시보드 통계
- **DB 인덱스**: 검색/정렬 컬럼에 인덱스 설정
- **N+1 방지**: FetchJoin 사용
- **Connection Pool**: HikariCP
- **페이지네이션**: 모든 목록 API
- **비동기 처리**: 알림 발송 (Spring Events)

---

### JCloud 배포
```bash
# 1. SSH 접속
ssh -i key.pem ubuntu@113.198.66.68

# 2. Git Clone
git clone https://github.com/your-repo/ecommerce.git
cd ecommerce

# 3. 환경 변수 설정
nano .env

# 4. Docker Compose 실행
docker-compose up -d

# 5. 확인
curl http://113.198.66.68:10254/health
```

---

## 향후 개선 사항

### 성능
- Redis Cluster 구성
- Database Replication (Master-Slave)
- Elasticsearch 검색 엔진

### 기능
- firebase를 활용한 상품 추천 (AI/ML)
- 결제 연동 (PG사)
- 배송 추적 API

### 인프라
- Kubernetes 배포
- 로그 수집 (ELK Stack)
