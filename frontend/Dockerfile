FROM node:18-alpine

WORKDIR /app

# package.json 복사 및 의존성 설치
COPY package*.json ./
RUN npm install

# 소스 코드 복사
COPY . .

# 빌드 인자로 받아서 환경변수로 설정
ARG REACT_APP_BACKEND_URL
ARG REACT_APP_NAVER_CLIENT_ID

ENV REACT_APP_BACKEND_URL=$REACT_APP_BACKEND_URL
ENV REACT_APP_NAVER_CLIENT_ID=$REACT_APP_NAVER_CLIENT_ID

EXPOSE 3000

CMD ["npm", "start"]