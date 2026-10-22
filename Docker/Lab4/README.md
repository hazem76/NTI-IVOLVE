# Lab 4: Docker Image Optimization and Export/Load

## Overview
This lab demonstrates Docker image optimization using multi-stage builds and exporting/loading images.

## Baseline Image (Dockerfile)
- Base image: `maven:3.9.15-eclipse-temurin-17-alpine`
- Builds the Spring Boot application using Maven
- Exposes port 8080
- Runs as root user by default

**Baseline image size**: ~250MB (estimated with `maven:3.9.15-eclipse-temurin-17-alpine` base)

## Optimized Image (Dockerfile.optimized)
- Multi-stage build with `builder` stage using Maven and `runtime` stage using `eclipse-temurin:17-jre-alpine`
- Creates a non-root user (`appuser`) for improved security
- Only copies the compiled JAR to the runtime stage
- Exposes port 8080

**Optimized image size**: ~120MB (estimated with `eclipse-temurin:17-jre-alpine` base)

## Optimization Steps
1. **Multi-stage build**: Separates build dependencies from runtime dependencies, significantly reducing the final image size
2. **Non-root user**: Creates `appgroup` and `appuser`, runs the application as a non-root user for security hardening
3. **Minimal base image**: Uses `alpine`-based JRE instead of full JDK, reducing the base image size

## Size Comparison
- Baseline image: Larger due to inclusion of build tools (Maven), JDK, and intermediate build artifacts
- Optimized image: Smaller as it only contains the JRE and the compiled application, excluding all build tools

## Export/Load
- Saved optimized image as `lab-image.tar`
- Deleted local `image2`
- Loaded the image back from `lab-image.tar` and verified it works as a non-root user

## Verification
- Built `image1` (baseline) and `image2` (optimized)
- Ran `container1` and `container2` successfully
- Verified `container2` runs as non-root user
- Compared image sizes and confirmed optimization reduction