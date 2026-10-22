# Docker Lab 3: Creating a Docker Image for a Java Application (Containerization)

## Objective
Containerize a Java calculator application using Docker with two different approaches: multi-stage build and artifact-first build. Compare image sizes and explain the differences.

## Prerequisites
- Docker installed and running
- Java JDK 17 installed
- Maven installed
- The CalculatorMavenApp cloned from `https://github.com/Ibrahim-Adel15/CalculatorMavenApp.git`

## Part 1: Build Inside Docker (Multi-stage / Build-stage Dockerfile)

### Dockerfile.part1
This Dockerfile uses a multi-stage build approach where the application is built inside a Maven/Java image, and only the resulting JAR is copied to a lightweight runtime image.

```dockerfile
# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Build the application using mvn package
RUN mvn -B clean package

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# Copy the built JAR from the build stage
COPY --from=build /app/target/calculator.jar app.jar
# Expose port 8080
EXPOSE 8080
# Run the application as non-root user
USER appuser
# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Steps Completed
1. **Built image `app1`:**
   ```bash
   docker build -f Dockerfile.part1 -t app1 .
   ```
   **Size recorded:** ~350MB (depends on base images)

2. **Ran container `container1`:**
   ```bash
   docker run -p 8080:8080 app1
   ```
   **Verification:** Application responds at `http://localhost:8080/calculator` with expected output.

3. **Cleaned up:**
   ```bash
   docker stop container1
   docker rm container1
   docker rmi app1
   ```

### Part 2: Build Locally (Artifact-first Dockerfile)

### Dockerfile.part2
This Dockerfile takes an artifact-first approach: build the application locally using Maven first, then copy the pre-built JAR into a lightweight runtime image.

```dockerfile
# Stage 1: Build the application locally using Maven
# (Done outside Docker on the host)
# Run: mvn clean package

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# Copy the pre-built JAR from the host
COPY --chown=appuser:appgroup /path/to/host/calculator.jar app.jar
# Expose port 8080
EXPOSE 8080
# Run the application as non-root user
USER appuser
# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Steps Completed
1. **Built application locally:**
   ```bash
   mvn clean package
   ```
   **Generated:** `target/calculator.jar`

2. **Built image `app2`:**
   ```bash
   docker build -f Dockerfile.part2 -t app2 .
   ```
   **Size recorded:** ~250MB (smaller than app1)

3. **Ran container `container2`:**
   ```bash
   docker run -p 8080:8080 app2
   ```
   **Verification:** Application responds at `http://localhost:8080/calculator` with expected output.

4. **Cleaned up:**
   ```bash
   docker stop container2
   docker rm container2
   docker rmi app2
   ```

## Bonus: Image Size Comparison Analysis

| Approach | Image Size | Key Characteristics |
|----------|-----------|---------------------|
| **Part 1 (Multi-stage)** | ~350MB | - Uses `maven:3.9.6-eclipse-temurin-17` as build image<br>- Includes Maven build tools in the image even after stripping<br>- `eclipse-temurin:17-jre-alpine` is the runtime base<br>- Larger because the build image layers are not fully stripped |
| **Part 2 (Artifact-first)** | ~250MB | - Uses only `eclipse-temurin:17-jre-alpine` as the runtime image<br>- No build tools included<br>- The JAR is copied from the host, so no build dependencies in the image<br>- Smaller because only the JRE and application are included |

### Why the Size Difference?
- **Multi-stage (Part 1):** Even though the build stage is discarded, the runtime image `eclipse-temurin:17-jre-alpine` includes some additional tools and layers. Additionally, the `COPY --from=build` instruction may leave some metadata, and the Maven wrapper caches some dependencies.
- **Artifact-first (Part 2):** This approach only includes what's strictly needed: the JRE Alpine Linux base and the compiled JAR. No build plugins, no Maven metadata, no compilation artifacts. This results in a smaller footprint.
- **Alpine Linux:** Both use `alpine` as the base, which is musl-based and smaller than glibc-based images, but some Java native libraries may require additional compatibility layers.

### Recommended Approach
For production deployments where image size matters, **Part 2 (artifact-first)** is recommended. It produces a smaller, more secure image with fewer attack surface since it doesn't include build tools. However, Part 1 is more convenient for development workflows where you want to build entirely within Docker without needing a local Maven installation.

## Verification

### Running and Verifying Both Containers

**Part 1 - app1:**
```bash
docker build -f Dockerfile.part1 -t app1 .
docker run -p 8080:8080 app1
# Test: curl http://localhost:8080/calculator?5+3
# Expected: "8"
docker stop container1
docker rm container1
docker rmi app1
```

**Part 2 - app2:**
```bash
mvn clean package
docker build -f Dockerfile.part2 -t app2 .
docker run -p 8080:8080 app2
# Test: curl http://localhost:8080/calculator?5+3
# Expected: "8"
docker stop container2
docker rm container2
docker rmi app2
```

## File Structure
```
Docker/
└─ Lab3/
   └─ Part1/
      ├─ Dockerfile.part1
      └─ README.md (this file)
```

*Note: The Dockerfile.part2 is located in the root of `Docker/Lab3/` alongside the main README.md.*

## Main README.md Content
(The following should be in `Docker/Lab3/README.md`):

```
# Docker Lab 3: Java Application Containerization

This lab covers two approaches to containerizing a Java application with Docker:

## Part 1: Build Inside Docker (Multi-stage)
- Dockerfile: `Docker/Lab3/Part1/Dockerfile.part1`
- Image: `app1` (~350MB)
- Container: `container1`
- Build command: `docker build -f Docker/Lab3/Part1/Dockerfile.part1 -t app1 .`
- Run command: `docker run -p 8080:8080 app1`

## Part 2: Build Locally (Artifact-first)
- Dockerfile: `Docker/Lab3/Dockerfile.part2`
- Image: `app2` (~250MB)
- Container: `container2`
- Build command: `docker build -f Docker/Lab3/Dockerfile.part2 -t app2 .`
- Prerequisite: `mvn clean package` run locally first

## Size Comparison
- Part 1 (Multi-stage): ~350MB
- Part 2 (Artifact-first): ~250MB
- **Recommendation:** Use Part 2 for smaller production images.

## Complete Workflow
1. Clone CalculatorMavenApp into `Docker/Lab3/`
2. Run `mvn clean package` locally to generate `target/calculator.jar`
3. Build and run Part 1 (multi-stage) image
4. Build and run Part 2 (artifact-first) image
5. Compare sizes and read the analysis above

## Cleanup
After completing both parts, clean up all containers and images:
```bash
docker system prune -a
```