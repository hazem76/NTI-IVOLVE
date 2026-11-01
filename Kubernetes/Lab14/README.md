# Lab 14: StatefulSet with Headless Service

## Overview
This lab demonstrates deploying a stateful MySQL database using a StatefulSet with a headless service for stable network identity and persistent storage.

## Components

### Headless Service (ivolveDBSvc)
- **Name**: `ivolveDBSvc`
- **Namespace**: `ivolve`
- **Type**: Headless (`clusterIP: None`)
- **Port**: 3306 (MySQL)
- **Selector**: `app: ivolveDB`
- **Purpose**: Provides stable DNS names for StatefulSet pods (e.g., `ivolveDB-0.ivolveDBSvc.ivolve.svc.cluster.local`)

### StatefulSet (ivolveDB)
- **Name**: `ivolveDB`
- **Namespace**: `ivolve`
- **Replicas**: 1
- **Service Name**: `ivolveDBSvc` (links to headless service)
- **Image**: `mysql:8.0`
- **Node Selector**: `project=ivolve`
- **Toleration**: `workload=ivolveApp:NoSchedule`
- **Environment Variables**: From `ivolveCM` ConfigMap and `ivolveSecret` Secret
- **Persistent Storage**: `volumeClaimTemplates` for `/var/lib/mysql` (1Gi, ReadWriteOnce)
- **Probes**: Readiness and liveness probes using `mysqladmin ping`

## Deployment

```bash
# Apply the service and StatefulSet
kubectl apply -f service.yaml
kubectl apply -f statefulset.yaml

# Verify StatefulSet
kubectl get statefulset ivolveDB -n ivolve

# Verify pods
kubectl get pods -n ivolve -l app=ivolveDB

# Verify headless service
kubectl get svc ivolveDBSvc -n ivolve

# Check pod DNS resolution
kubectl exec -it ivolveDB-0 -n ivolve -- nslookup ivolveDBSvc

# Check persistent volume claim
kubectl get pvc -n ivolve
```

## Verification

Expected output after successful deployment:

```
NAME        READY   AGE
ivolveDB    1/1     2m

NAME          READY   STATUS    RESTARTS   AGE
ivolveDB-0    1/1     Running   0          2m

NAME           TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
ivolveDBSvc    ClusterIP   None         <none>        3306/TCP   2m

NAME                              STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
mysql-data-ivolveDB-0             Bound    pvc-xxx                                    1Gi        RWO            standard       2m
```

## Validation Commands

```bash
# Test MySQL connectivity from within the pod
kubectl exec -it ivolveDB-0 -n ivolve -- mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;"

# Verify database initialization from ConfigMap/Secret
kubectl exec -it ivolveDB-0 -n ivolve -- mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "USE ivolve; SHOW TABLES;"

# Check StatefulSet persistent identity
kubectl exec -it ivolveDB-0 -n ivolve -- hostname
# Should output: ivolveDB-0
```

## Notes
- Headless service (`clusterIP: None`) creates DNS records for each pod
- `volumeClaimTemplates` automatically creates PVCs for each replica
- StatefulSet provides ordered deployment, scaling, and rolling updates
- MySQL environment variables are sourced from existing ConfigMap (`ivolveCM`) and Secret (`ivolveSecret`) in the `ivolve` namespace
- Node selector and toleration ensure the pod runs on the designated node (`desktop-control-plane`)