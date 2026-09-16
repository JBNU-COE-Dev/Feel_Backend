# 1. 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B package -DskipTests

# 2. 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads && chmod 755 /app/uploads

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod TZ=Asia/Seoul

EXPOSE 8080

# 서비스 상태 체크 (실제 존재하는 엔드포인트여야 함)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#   CMD wget -q -O /dev/null "http://127.0.0.1:8080/api/notices?page=0&size=1" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
