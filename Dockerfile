# 1. JDK 21이 포함된 경량 베이스 이미지
FROM openjdk:21-jdk-slim

# 2. 타임존 설정 (한국)
ENV TZ=Asia/Seoul

# 3. 빌드 타임 아규먼트 추가 → 항상 레이어가 새로 빌드되도록
ARG BUILD_TIME
ENV BUILD_TIME=${BUILD_TIME}

# 4. 로컬에서 빌드된 JAR 파일을 컨테이너 안으로 복사
COPY build/libs/Dashboard-0.0.1-SNAPSHOT.jar app.jar

# 5. 컨테이너 실행 명령
ENTRYPOINT ["java","-jar","/app.jar"]
