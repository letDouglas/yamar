# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies offline. This layer is cached unless pom.xml changes.
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Package the application, skipping tests to speed up the container build
# (Tests are assumed to be run in the CI/CD pipeline prior to this step)
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

# Create a non-root user group and user for security hardening
RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership of the application artifacts to the non-root user
RUN chown -R yamar:yamar /app

# Switch to the non-root user
USER yamar

# Expose the Gateway port
EXPOSE 9090

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]