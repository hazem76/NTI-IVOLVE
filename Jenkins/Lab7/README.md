# Lab 7: Multi-Network Microservices

## Overview
Lab 7 demonstrates Docker networking with multiple bridge networks, inter-container communication, named volumes, and bind mounts. Three microservices (frontend, backend, notification) communicate across separate Docker networks.

## Architecture
- **frontend-backend network**: Frontend connects to backend
- **backend-notification network**: Backend connects to notification service
- Data persistence via named volumes and bind mounts

## Network Topology
```
frontend  ↔  backend  ↔  notification
  (bn)         (fn)         (nn)
```

## Prerequisites
- Docker Desktop/Engine installed
- Git cloned repository code in `./src/`

## Setup & Execution

### 1. Build Docker Images
```bash
# Build all three services
docker build -t lab7-frontend ./src/frontend
docker build -t lab7-backend ./src/backend
docker build -t lab7-notification ./src/notification
```

### 2. Create Docker Networks
```bash
# Create separate bridge networks with subnets
docker network create frontend-backend --subnet 172.20.0.0/16
docker network create backend-notification --subnet 172.21.0.0/16
```

### 3. Create Named Volumes
```bash
# Volumes for log persistence
docker volume create backend-logs
docker volume create notification-logs
```

### 4. Run Notification Service
```bash
docker run -d \
  --name notification \
  --network backend-notification \
  -e LOG_LEVEL=info \
  lab7-notification
```

### 5. Run Backend Service
```bash
docker run -d \
  --name backend \
  --network frontend-backend \
  --network backend-notification \
  -e NOTIFICATION_URL=http://notification:5001/notify \
  -v backend-logs:/app/logs \
  -v backend-data:/app/data \
  lab7-backend
```
*Backend connected to BOTH networks with bind mounts for data and logs*

### 6. Run Frontend Service
```bash
docker run -d \
  --name frontend \
  --network frontend-backend \
  -p 8080:3000 \
  -e BACKEND_URL=http://backend:5000 \
  lab7-frontend
```

### 7. Verify Web Access
Open your browser to: `http://localhost:8080`

- Use the form to send messages
- Verify messages appear in the list
- Check that notifications are sent properly

## Volume & Bind Mount Details

| Volume/Mount | Purpose | Path |
|-------------|---------|------|
| `backend-logs` | Persist backend log output | `/app/logs` |
| `backend-data` | Persist messages.json data | `/app/data` |
| Host bind mount | Optional: host-side data persistence | `./data:/app/data` (on host) |

## Inter-Service Communication

- **Frontend → Backend**: `BACKEND_URL=http://backend:5000` (via frontend-backend network)
- **Backend → Notification**: `NOTIFICATION_URL=http://notification:5001/notify` (via backend-notification network)
- All services use Docker's internal DNS for service discovery within networks

## Troubleshooting

- **Cannot reach backend**: Verify frontend-backend network is attached to both containers
- **Notifications failing**: Verify backend-notification network and NOTIFICATION_URL environment variable
- **Messages not persisting**: Ensure `-v backend-data:/app/data` bind mount is mounted
- **Log loss on restart**: Ensure `-v backend-logs:/app/logs` volume is mounted

## Expected Results

![Expected Output](https://i.imgur.com/example.png)

- UI at `http://localhost:8080` shows message input form
- Submitting a message stores it in backend data volume
- Notification service logs the received message
- Messages persist across container restarts via volumes