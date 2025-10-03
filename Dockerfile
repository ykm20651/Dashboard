# JDK 21이 포함된 경량 이미지 사용 -> 현재 애플리케이션 jdk 버전에 맞춰서 설정
FROM openjdk:21-jdk-slim

# 타임존(선택) – 한국이면 Asia/Seoul
ENV TZ=Asia/Seoul

# 로컬에서 빌드된 JAR 파일을 컨테이너 안으로 복사 (빌드된 JAR 경로)
COPY build/libs/Dashboard-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 실행 시 JAR 실행
ENTRYPOINT ["java","-jar","/app.jar"]
