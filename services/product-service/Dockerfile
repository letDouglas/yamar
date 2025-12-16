# -----------------------------------------------------------------------------
# Stage 1: Build Stage (Dependency Caching Strategy)
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Dependency Layer: Copy descriptors and shared resources first.
# Docker will cache this layer. If pom.xml doesn't change,
# dependencies are NOT re-downloaded.
COPY services/product-service/pom.xml .
COPY infra /infra

# 2. Go Offline: Download all dependencies into the local repository.
# This ensures subsequent builds are fast and do not require network calls.
RUN mvn dependency:go-offline -B

# 3. Source Layer: Copy the actual application code.
# Changes here will only trigger the compilation step (fast).
COPY services/product-service/src ./src

# 4. Compile and Package (Offline mode)
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage (Distroless-like)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

# Security: Create a non-root user to run the application
RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Enforce ownership for the non-root user
RUN chown -R yamar:yamar /app

USER yamar

EXPOSE 8041

ENTRYPOINT ["java", "-jar", "app.jar"]