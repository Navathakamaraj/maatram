# Multi-stage build for Spring Boot Backend
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/maatram-backend-0.1.0.jar app.jar

ENV PORT=8000
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=maatram
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
