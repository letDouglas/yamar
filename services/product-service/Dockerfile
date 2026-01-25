# -----------------------------------------------------------------------------
# STAGE 1: BUILD & PACKAGE
# -----------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -pl services/product-service -am -DskipTests

# -----------------------------------------------------------------------------
# STAGE 2: RUNTIME ENVIROMENT
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

RUN addgroup -S yamar && adduser -S yamar -G yamar

WORKDIR /app

COPY --from=builder /app/services/product-service/target/*.jar app.jar

RUN chown -R yamar:yamar /app

USER yamar

EXPOSE 8041

ENTRYPOINT ["java", "-jar", "app.jar"]