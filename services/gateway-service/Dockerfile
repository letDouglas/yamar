# -----------------------------------------------------------------------------
# Stage 1: Build Stage (Dependency Caching Strategy)
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Dependency Layer
COPY services/gateway-service/pom.xml .
# Gateway typically doesn't depend on shared infra/avro schemas,
# but we keep the structure consistent if needed.
# COPY infra /infra

# 2. Go Offline: Cache dependencies
RUN mvn dependency:go-offline -B

# 3. Source Layer
COPY services/gateway-service/src ./src

# 4. Compile
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

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]