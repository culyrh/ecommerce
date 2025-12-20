- http://113.198.66.68:10230
- http://113.198.66.68:10230/health
- http://113.198.66.68:10230/swagger-ui.html

<br>

### 1

- gradlew 실행 권한 문제
  - -> gradlew에 chmod +x / gradle 명령어 추가

- Gradle wrapper 타임아웃
  - -> gradle 8.5로 버전 수정

- curl 설치 실패 
  - -> 도커파일에서 Health check 제거하고 docker-compose로 실행

- SSH 포트 
  - -> 19230 설정

- OAuth2 리다이렉트 
  - -> SecurityConfig 추가하여 임시로 접근 허용
  
- GitHub Actions 
  - -> (ci) Deploy to JCloud 설정하여 깃 변경사항이 서버에 바로 반영되도록 함

### 2