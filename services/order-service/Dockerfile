# -----------------------------------------------------------------------------
# Stage 1: Build Stage (Dependency Caching Strategy)
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Dependency Layer: Copy descriptors and shared resources.
COPY services/order-service/pom.xml .
COPY infra /infra

# 2. Go Offline: Download dependencies to leverage Docker Cache.
RUN mvn dependency:go-offline -B

# 3. Source Layer: Copy application logic.
COPY services/order-service/src ./src

# 4. Compile and Package
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

# Security: Run as non-root
RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R yamar:yamar /app

USER yamar

EXPOSE 8042

ENTRYPOINT ["java", "-jar", "app.jar"]