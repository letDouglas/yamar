# -----------------------------------------------------------------------------
# Stage 1: Build Stage (Dependency Caching Strategy)
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Dependency Layer
COPY services/user-service/pom.xml .
COPY infra /infra

# 2. Go Offline: Cache dependencies
RUN mvn dependency:go-offline -B

# 3. Source Layer
# CORREZIONE QUI: Deve essere user-service
COPY services/user-service/src ./src

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

# CORREZIONE QUI: Porta 8044
EXPOSE 8044

ENTRYPOINT ["java", "-jar", "app.jar"]