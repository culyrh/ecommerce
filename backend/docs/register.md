### Oauth

####  Google OAuth2 플로우

```
1. Authorization Code 받기 (프론트엔드)
2. Code → Access Token 교환 (백엔드)
3. Access Token → 사용자 정보 조회
4. 계정 생성/조회 → JWT 발급
```

#### Firebase Auth 플로우

```
1. Firebase ID Token 받기 (프론트엔드)
2. ID Token 검증 (백엔드 - FirebaseAuth.verifyIdToken)
3. 사용자 정보 추출 (email, name, uid)
4. 계정 생성/조회 → JWT 발급
```

<br>

### 이메일 중복 방지

#### Email 유니크 제약

- 같은 이메일로 일반 회원가입을 2번 할 수 없습니다. (409 에러)

- 하지만 일반 가입 후, 같은 이메일로 구글 로그인을 시도하면 기존 계정에 소셜 정보가 추가되는 방식으로 통합됩니다.
  - **결과적으로 하나의 계정에 일반 로그인과 구글 로그인 둘 다 가능해집니다.**

(1) 일반 가입 -> 구글 로그인: 계정 통합

(2) 구글 로그인 -> 일반 가입: `existByEmail()`체크 후 '이미 사용중인 이메일입니다' 에러

<br>

#### 

#### 소셜 계정 연동

- 중복 방지: provider + providerId 유니크 제약

```
// OAuth2Service.java
private User findOrCreateUser(OAuth2UserInfo userInfo) {
    // 1. 소셜 계정으로 찾기
    Optional<User> existingUser = userRepository.findByProviderAndProviderId(...);
    
    // 2. 이메일로 기존 계정 찾기
    Optional<User> userByEmail = userRepository.findByEmail(...);
    if (userByEmail.isPresent()) {
        // 기존 계정에 소셜 정보 연동 (같은 이메일이면 소셜 정보만 추가)
        user.setProvider(userInfo.getProvider());
        user.setProviderId(userInfo.getProviderId());
    }
    
    // 3. 신규 가입자는 회원 생성
    User newUser = User.builder()...
}
```

<br>

#### User 테이블 구조

```
// User.java
- password: nullable (소셜 로그인 사용자는 null)
- provider: "GOOGLE", "FIREBASE", null (일반 가입)
- providerId: 소셜 플랫폼 고유 ID
- role: ROLE_USER (기본값)
- is_active: true (소프트 삭제 플래그)
```

<br>

#### 유니크 제약

- 이메일 유니크: 중복 가입 방지
- 소셜 계정 유니크: (provider, providerId)

```
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "unique_provider_id", 
                     columnNames = {"provider", "provider_id"})
})
```

<br>

#### 인덱스

- 이메일 인덱스 (로그인 시 빠른 조회)
- 역할 인덱스 (권한 체크)

```
@Index(name = "idx_users_email", columnList = "email")
@Index(name = "idx_users_role", columnList = "role")
```
