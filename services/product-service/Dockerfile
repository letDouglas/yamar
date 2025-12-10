# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies offline
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

# Create a non-root user for security hardening
RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown -R yamar:yamar /app

# Switch to the non-root user
USER yamar

# Expose the Product Service port
EXPOSE 8041

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]