# --- GIAI ĐOẠN 1: Builder ---
# Giữ nguyên maven:3.9, chỉ sửa số 17 thành 21
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# --- GIAI ĐOẠN 2: Runner ---
# Sửa số 17 thành 21
FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]