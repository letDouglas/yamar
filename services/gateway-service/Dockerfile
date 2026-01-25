# -----------------------------------------------------------------------------
# STAGE 1: BUILD & PACKAGE
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy full monorepo context to resolve Parent POM relative paths correctly.
# Note: .dockerignore filters out unnecessary files (target, .git, etc).
COPY . .

# Compile specific module and its dependencies.
# -pl : Project List (build only this service)
# -am : Also Make (build required dependencies/parent)
RUN mvn clean package -pl services/gateway-service -am -DskipTests

# -----------------------------------------------------------------------------
# STAGE 2: RUNTIME ENVIROMENT
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

# Create non-root user for security compliance
RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

# Retrieve the executable JAR from the builder stage
COPY --from=builder /app/services/gateway-service/target/*.jar app.jar

# Enforce file ownership
RUN chown -R yamar:yamar /app

# Drop root privileges
USER yamar

# Expose Service Port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]