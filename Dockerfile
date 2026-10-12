# ==============================================================================
# Multi-Stage Production Dockerfile for Online Food Ordering System
# ==============================================================================

# Stage 1: Build JAR with Maven & Eclipse Temurin JDK 17
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN mvn clean package -DskipTests=false

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add unprivileged user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy jar from build stage
COPY --from=build /app/target/food-ordering-system-1.0.0.jar app.jar

# Environment defaults
ENV SERVER_PORT=8080
ENV MONGODB_URI=mongodb://localhost:27017/food_ordering_db
ENV JWT_EXPIRATION_MS=86400000

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
