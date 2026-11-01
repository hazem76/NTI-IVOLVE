# Lab 16: Pod Resource Management

## Overview
This lab updates the `ivolveApp` Deployment (from Lab 15) with explicit resource requests and limits for both the init container and main application container, enabling QoS guarantees and resource-aware scheduling.

## Resource Specifications

### Main Container (ivolve-app)
| Resource | Request | Limit |
|----------|---------|-------|
| CPU      | 1       | 2     |
| Memory   | 1Gi     | 2Gi   |

### Init Container (init-db)
| Resource | Request | Limit |
|----------|---------|-------|
| CPU      | 250m    | 500m  |
| Memory   | 256Mi   | 512Mi |

## Quality of Service (QoS) Class
With both requests and limits specified, the pod gets **Guaranteed** QoS class (highest priority):
- Requests = Limits for all resources → Guaranteed
- Ensures pod is not evicted under resource pressure
- Enables proper resource scheduling and capacity planning

## Deployment

```bash
# Apply the updated deployment
kubectl apply -f deployment-updated.yaml

# Verify deployment update
kubectl rollout status deployment/ivolveApp -n ivolve

# Check pod resource specifications
kubectl get pods -n ivolve -l app=ivolveApp -o custom-columns=NAME:.metadata.name,CPU_REQ:.spec.containers[0].resources.requests.cpu,CPU_LIM:.spec.containers[0].resources.limits.cpu,MEM_REQ:.spec.containers[0].resources.requests.memory,MEM_LIM:.spec.containers[0].resources.limits.memory
```

## Verification

### 1. kubectl describe pod
```bash
kubectl describe pod -n ivolve -l app=ivolveApp
```

Expected output includes:
```
Containers:
  ivolve-app:
    Requests:
      cpu:        1
      memory:     1Gi
    Limits:
      cpu:        2
      memory:     2Gi
    QoS Class:       Guaranteed
  init-db:
    Requests:
      cpu:        250m
      memory:     256Mi
    Limits:
      cpu:        500m
      memory:     512Mi
    QoS Class:       Guaranteed
```

### 2. kubectl top pod (requires metrics-server)
```bash
# Install metrics-server if not present
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Wait for metrics-server to be ready, then:
kubectl top pod -n ivolve -l app=ivolveApp
```

Expected output:
```
NAME                          CPU(cores)   MEMORY(bytes)
ivolveApp-xxx-yyy             15m          120Mi
ivolveApp-xxx-zzz             12m          115Mi
```

### 3. Resource Quota Verification (from Lab 11)
```bash
kubectl describe resourcequota ivolve-quota -n ivolve
```

Should show resource consumption within quota limits.

## Resource Management Best Practices Demonstrated

1. **Requests**: Minimum resources guaranteed to the container (used for scheduling)
2. **Limits**: Maximum resources the container can consume (enforced by cgroups)
3. **Guaranteed QoS**: Requests = Limits → Highest priority, no eviction
4. **Init Container Resources**: Separate limits prevent init from starving main container
5. **Capacity Planning**: Total requests = 2 cores CPU, 2Gi memory per pod × 2 replicas = 4 cores, 4Gi

## Notes
- The `Guaranteed` QoS class ensures pods are never evicted due to resource pressure
- Resource requests influence pod scheduling (pods placed on nodes with sufficient capacity)
- Limits prevent noisy neighbor problems (one pod consuming all node resources)
- Metrics-server must be installed for `kubectl top` to work
- In Docker Desktop, the control plane node has limited resources; adjust values if scheduling fails