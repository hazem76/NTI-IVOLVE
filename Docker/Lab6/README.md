# Lab 6: Docker Environment Variables and Resource Management

## Overview
Lab 6 demonstrates Docker environment variable management and resource constraints. This lab covers running containers with environment variables, loading variables from files, resource limiting (CPU and memory), and application verification.

## Project Structure
```
Docker/Lab6/
├── src/                  ← Python application source files
│   ├── app.py            ← Flask Python application
│   └── requirements.txt  ← Flask==3.1.2
├── Dockerfile            ← Python 3.12-slim Dockerfile
├── env.txt               ← Environment variables (APP_NAME=iVolveApp2, APP_ENV=prod)
├── requirements.txt      ← Python dependencies (at lab level, redundant with src/)
└── README.md             ← Environment configs, constraints, and monitoring
```

## Dockerfile Configuration
```dockerfile
# Use Python 3.12 slim base image
FROM python:3.12-slim

# Set working directory
WORKDIR /app

# Copy requirements and install dependencies
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app.py .

# Expose port 5000
EXPOSE 5000

# Run the application
CMD ["python", "app.py"]
```

## Lab 6 Tasks

### Task 1: Run Container with Environment Variables and Resource Limits
```bash
# Run container with environment variables and resource limits
docker run -d \
  --name container1 \
  -p 5000:5000 \
  -e APP_NAME=iVolveApp1 \
  -e APP_ENV=stg \
  --cpus=1 \
  --memory=256m \
  ivolve1:latest
```

### Verification
```bash
docker ps
docker exec container1 printenv | grep -E "APP_NAME|APP_ENV"
docker stats container1
```

### Expected Output
```
CONTAINER ID   IMAGE           COMMAND                  CREATED          STATUS          PORTS                    NAMES
<container_id>   ivolve1:latest   "/__cacert_entrypoint..."   10 seconds ago   Up <seconds>   0.0.0.0:5000->5000/tcp   container1

APP_NAME=iVolveApp1
APP_ENV=stg

# docker stats output showing CPU and memory usage within limits
```

### Task 2: Run Container Loading Variables from env.txt
```bash
# Create environment variable file
echo "APP_NAME=iVolveApp2" > env.txt
echo "APP_ENV=prod" >> env.txt

# Run container loading variables from env.txt file
docker run -d \
  --name container2 \
  -p 5000:5000 \
  --env-file env.txt \
  --cpus=1 \
  --memory=256m \
  ivolve1:latest
```

### Verification
```bash
docker ps
docker exec container2 printenv | grep -E "APP_NAME|APP_ENV"

# Expected output:
APP_NAME=iVolveApp2
APP_ENV=prod
```

### Task 3: Hardcode Environment Variables in Dockerfile
```dockerfile
# Use Python 3.12 slim base image
FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .

# Hardcode environment variables
ENV APP_NAME=iVolveApp3
ENV APP_ENV=dev

EXPOSE 5000

CMD ["python", "app.py"]
```

### Build and Run
```bash
docker build -t ivolve1:dev .

docker run -d \
  --name container3 \
  -p 5000:5000 \
  ivolve1:dev
```

### Verify Hardcoded Variables
```bash
docker exec container3 printenv | grep -E "APP_NAME|APP_ENV"

# Expected output:
APP_NAME=iVolveApp3
APP_ENV=dev
```

## README.md Contents
The README.md covers:
- Environment variable configuration patterns
- How to run containers with `-e` flags
- How to use `--env-file` for loading variables from files
- Resource limiting with `--cpus` and `--memory`
- How to verify variables are properly loaded
- Differences between runtime vs. Dockerfile ENV settings
- Docker stats monitoring output examples
- Troubleshooting tips for common environment variable issues