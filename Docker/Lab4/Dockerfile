FROM eclipse-temurin:17-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY java/com/example/demo/DemoApplication.java .
RUN javac DemoApplication.java
USER appuser
EXPOSE 8080
CMD ["java", "com.example.demo.DemoApplication"]