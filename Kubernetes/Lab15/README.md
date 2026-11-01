# Lab 15: Node.js App Deployment with ClusterIP Service

## Overview
This lab deploys the Node.js application (from Lab 8) as a Kubernetes Deployment with 2 replicas, using a ClusterIP service for internal access. The deployment includes an init container for database initialization and mounts the persistent volume from Lab 13.

## Components

### Deployment (ivolveApp)
- **Name**: `ivolveApp`
- **Namespace**: `ivolve`
- **Replicas**: 2
- **Image**: `hazem7777/nodejs-docker-exercise:prod` (Docker Hub image from Lab 8)
- **Node Selector**: `project=ivolve`
- **Toleration**: `workload=ivolveApp:NoSchedule`
- **Port**: 3000 (HTTP)

#### Init Container (init-db)
- **Image**: `mysql:8.0` (MySQL client)
- **Purpose**: Waits for database readiness, then creates `ivolve` database and grants privileges to `ivolve` user
- **Environment**: Sources `MYSQL_ROOT_PASSWORD` from `ivolveSecret`
- **Target**: Connects to `ivolveDBSvc` (headless service from Lab 14)

#### Main Container (ivolve-app)
- **Environment Variables**: From `ivolveCM` ConfigMap and `ivolveSecret` Secret
- **Volume Mount**: `app-logs` at `/app/logs` (bound to `ivolvePVC` from Lab 13)
- **Probes**:
  - Readiness: `GET /ready` on port 3000
  - Liveness: `GET /health` on port 3000

### Service (ivolveAppSvc)
- **Name**: `ivolveAppSvc`
- **Namespace**: `ivolve`
- **Type**: `ClusterIP`
- **Port**: 3000 (targetPort: 3000)
- **Selector**: `app: ivolveApp`
- **Purpose**: Internal load balancing across the 2 replicas

## Deployment

```bash
# Apply the deployment and service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Verify deployment
kubectl get deployment ivolveApp -n ivolve

# Verify pods
kubectl get pods -n ivolve -l app=ivolveApp

# Verify service
kubectl get svc ivolveAppSvc -n ivolve

# Check init container logs
kubectl logs -n ivolve -l app=ivolveApp -c init-db

# Check main container logs
kubectl logs -n ivolve -l app=ivolveApp -c ivolve-app
```

## Verification

Expected output after successful deployment:

```
NAME         READY   UP-TO-DATE   AVAILABLE   AGE
ivolveApp    2/2     2            2           3m

NAME                          READY   STATUS    RESTARTS   AGE
ivolveApp-xxx-yyy             1/1     Running   0          3m
ivolveApp-xxx-zzz             1/1     Running   0          3m

NAME            TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
ivolveAppSvc    ClusterIP   10.100.x.x     <none>        3000/TCP   3m
```

## Validation Commands

```bash
# Test application health endpoint via service DNS
kubectl run -it --rm curl-test --image=curlimages/curl -- \
  curl -s http://ivolveAppSvc.ivolve.svc.cluster.local:3000/health

# Test application ready endpoint
kubectl run -it --rm curl-test --image=curlimages/curl -- \
  curl -s http://ivolveAppSvc.ivolve.svc.cluster.local:3000/ready

# Verify database connectivity from app pod
kubectl exec -it -n ivolve deploy/ivolveApp -- \
  mysql -h ivolveDBSvc -u ivolve -ppassword -e "USE ivolve; SHOW TABLES;"

# Check persistent volume mount
kubectl exec -it -n ivolve deploy/ivolveApp -- ls -la /app/logs

# Port-forward for local testing
kubectl port-forward -n ivolve svc/ivolveAppSvc 3000:3000
# Then test: curl http://localhost:3000/health
```

## Notes
- The init container ensures the database is ready and properly configured before the app starts
- `ivolvePVC` (from Lab 13) provides shared storage for application logs across replicas
- Environment variables from `ivolveCM` and `ivolveSecret` (created in Lab 12) configure the application
- Node selector and toleration ensure pods schedule on the correct node (Docker Desktop control plane)
- ClusterIP service enables internal service-to-service communication within the cluster