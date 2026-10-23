# Lab 8: Docker Compose Stack - Node.js & MySQL

## Overview
Lab 8 demonstrates a complete Docker Compose configuration for a Node.js application with MySQL database persistence, custom networking, and volume-based data persistence.

## Architecture
- **app**: Node.js application (express server) connecting to MySQL
- **db**: MySQL database server creating and hosting the `ivolve` database
- **Network**: Custom bridge network `ivolve-net` for isolated communication

## docker-compose.yml Configuration

### Networks
- `ivolve-net`: Custom bridge network for service communication

### Volumes
- `db_data`: Persists MySQL data at `/var/lib/mysql`
- `app-logs`: Persists Node.js application logs at `/app/logs/`

### Services

#### App Service
- **Build**: From local Dockerfile in `./src/`
- **Ports**: Maps container port 3000 to host port 3000
- **Environment**: Database connection variables (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME)
- **Volumes**: Persists logs at `/app/logs/` via `app-logs` volume
- **Networks**: Connected to `ivolve-net`

#### Database Service
- **Image**: MySQL 8.0 official image
- **Environment**: Configures root password, database name, user credentials
- **Volumes**: Persists data at `/var/lib/mysql` via `db_data` volume
- **Networks**: Connected to `ivolve-net`

## Execution Steps

### 1. Start the Stack
```bash
docker compose up -d
```

### 2. Wait for MySQL Initialization
```bash
# Check MySQL is ready
docker compose logs db | grep -i "ready"

# Check app is running
docker compose logs app | grep -i "server started"
```

### 3. Verify Database Connectivity
```bash
# Check health endpoint
curl http://localhost:3000/health

# Check MySQL database via docker exec
docker exec -it $(docker compose ps -q db) mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SHOW DATABASES;"

# List tables in ivolve database
docker exec -it $(docker compose ps -q db) mysql -u root -p$MYSQL_ROOT_PASSWORD -e "USE ivolve; SHOW TABLES;"
```

### 4. Tag and Push to Docker Hub
```bash
# Replace with your Docker Hub username
DOCKER_ID=your-dockerhub-username

# Tag the image
docker tag lab8-node-mysql:$DOCKER_ID/lab8-node-mysql:latest

# Push to Docker Hub
docker push $DOCKER_ID/lab8-node-mysql:latest
```

### 4. Stop the Stack
```bash
docker compose down
```

## Persistence

| Data Type | Volume | Mount Path | Description |
|-----------|--------|------------|-------------|
| MySQL Data | `db_data` | `/var/lib/mysql` | Persists database tables, records, and schema across restarts |
| App Logs | `app-logs` | `/app/logs/` | Persists application log output for debugging and auditing |

## Environment Variables

### App Service Variables
| Variable | Value | Description |
|----------|-------|-------------|
| `DB_HOST` | `db` | Service name of the MySQL database |
| `DB_USER` | `root` | MySQL root username |
| `DB_PASSWORD` | `password` | MySQL root password |
| `DB_NAME` | `ivolve` | Database name to create and use |

### Database Service Variables
| Variable | Value | Description |
|----------|-------|-------------|
| `MYSQL_ROOT_PASSWORD` | `password` | Root user password for MySQL access |
| `MYSQL_DATABASE` | `ivolve` | Initial database name (created on first start) |
| `MYSQL_USER` | `ivolve_user` | Additional user with database access |
| `MYSQL_PASSWORD` | `ivolve_pass` | Password for the additional user |

## Verification Commands

```bash
# Verify app is running
docker compose ps

# Check service logs
docker compose logs app
docker compose logs db

# Test API endpoints
curl http://localhost:3000/health
curl http://localhost:3000/ready

# MySQL verification
docker exec -it $(docker compose ps -q db) mysql -u root -p -e "SELECT * FROM ivolve.products;"
```

## Cleanup

```bash
# Stop and remove containers, networks, volumes
docker compose down

# Optionally remove volumes (deletes all data)
docker compose down -v

# Remove dangling images
docker image prune -a
```

## Docker Hub Push Workflow

1. Build images: `docker compose up --build`
2. Tag: `docker tag lab8-node-mysql your-user/lab8-node-mysql:latest`
3. Push: `docker push your-user/lab8-node-mysql:latest`
4. Verify: Check Docker Hub repository for latest tag