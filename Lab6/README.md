# Lab 6: Docker Environment Variables and Resource Management

## Overview
This lab demonstrates Docker environment variable management and resource constraints for containerized Python applications.

## Dockerfile Configuration

### Base Image
- `python:3.12-slim` - Minimal Python 3.12 image based on Debian Slim

### Application Setup
- Working directory: `/app`
- Copies `requirements.txt` and installs Flask via pip
- Copies `app.py` source file
- Exposes port 5000

### Hardcoded Environment Variables (in Dockerfile)
- `APP_NAME=iVolveApp3`
- `APP_ENV=dev`

## Lab 6 - Configuration Scenarios

### Scenario 1: container1 (Running with Explicit Environment Variables)
- **Command**: `docker run -d -p 5000:5000 --name container1 --cpus 1 --memory 256m -e APP_NAME=iVolveApp1 -e APP_ENV=stg ivolve1`
- **Port mapping**: 5000 → 5000
- **Environment variables**:
  - `APP_NAME=iVolveApp1`
  - `APP_ENV=stg`
- **Resource limits**: 1 CPU, 256 MB memory
- **Verification**: 
  - Access `http://localhost:5000` to see application output
  - Access `http://localhost:5000/health` for health check
  - Use `docker stats container1` to monitor CPU and memory usage

### Scenario 2: container2 (Loading Variables from env.txt)
- **Create env.txt**: Contains `APP_NAME=iVolveApp2` and `APP_ENV=prod`
- **Command**: `docker run -d -p 5000:5000 --name container2 --cpus 1 --memory 256m --env-file env.txt ivolve1`
- **Environment variables loaded from file**: 
  - `APP_NAME=iVolveApp2`
  - `APP_ENV=prod`
- **Verification**: Same as container1, but with prod environment variables

### Scenario 3: Hardcoded in Dockerfile
- The Dockerfile itself defines `APP_NAME=iVolveApp3` and `APP_ENV=dev`
- **Command**: `docker run -d -p 5000:5000 --name container3 --cpus 1 --memory 256m ivolve1`
- **No additional -e flags needed** since variables are in the image
- **Verification**: Application runs with hardcoded dev environment

## Resource Monitoring

### docker stats
- `docker stats container1` displays real-time resource usage:
  - CPU percentage
  - Memory usage/limit
  - Network I/O
  - Block I/O

### Expected Output Format
```
CONTAINER ID   NAME                CPU %     MEM USAGE / LIMIT    MEM %     NET I/O      BLOCK I/O
abc123         container1          0.50      120Mi / 256Mi        47%       1.2kB / 800B   5.3kB / 2.1kB
```

## Key Concepts

### Environment Variable Sources
1. **Command-line `-e` flags**: Passed at `docker run` time
2. **`--env-file`**: Loaded from a `.txt` file (like `env.txt`)
3. **Dockerfile `ENV` instruction**: Hardcoded in the image build

### Resource Constraints
- **`--cpus`**: Limits CPU usage (e.g., `--cpus 1` = 1 full CPU core)
- **`--memory`**: Limits memory usage (e.g., `--memory 256m` = 256 MB)
- **OOM Killer**: If memory limit is exceeded, the OOM killer terminates the container

### Best Practices
- Use `.env` files for non-sensitive configuration
- Avoid hardcoding sensitive data (passwords, API keys) in Dockerfiles
- Always set resource limits in production to prevent resource exhaustion
- Use health checks to monitor container status