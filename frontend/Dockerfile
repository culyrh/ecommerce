# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

# 빌드 인자로 받기
ARG REACT_APP_BACKEND_URL
ARG REACT_APP_NAVER_CLIENT_ID

# 환경변수로 설정 (React가 빌드 시 사용)
ENV REACT_APP_BACKEND_URL=$REACT_APP_BACKEND_URL
ENV REACT_APP_NAVER_CLIENT_ID=$REACT_APP_NAVER_CLIENT_ID

# 프로덕션 빌드
RUN npm run build

# Production stage
FROM nginx:alpine

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 빌드된 파일 복사
COPY --from=build /app/build /usr/share/nginx/html

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]