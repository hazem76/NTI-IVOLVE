FROM maven:3.9.15-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn compile -DskipTests

FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=builder /app/target/classes ./
USER appuser
EXPOSE 8080
CMD ["java", "com.example.demo.DemoApplication"]