# Lab 11: Namespace Management and Resource Quota Enforcement

## Objective
Demonstrate namespace isolation and resource quota enforcement in Kubernetes.

## Setup

### Create Namespace
Create the `ivolve` namespace:

```bash
kubectl create namespace ivolve
```

### Verify Namespace Creation
```bash
kubectl get namespaces ivolve
```

## Resource Quota

### Create ResourceQuota Manifest
Create a `ResourceQuota` named `ivolveQuota` in the `ivolve` namespace limiting total pods to `3`:

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: ivolveQuota
  namespace: ivolve
spec:
  hard:
    pods: "3"
```

Apply the quota:
```bash
kubectl apply -f quota.yaml
```

### Verify Resource Quota
```bash
kubectl describe resourcequota ivolveQuota -n ivolve
```

Expected output sections:
- **Name**: ivolveQuota
- **Namespace**: ivolve
- **Hard Limits**:
  - `pods`: 3
  - (other resources can be added: `cpu`, `memory`, `persistentvolumeclaims`, etc.)

### Create Pods Within Quota Limit
Create pods that count against the quota:

```bash
kubectl run pod1 --image=nginx -n ivolve
kubectl run pod2 --image=nginx -n ivolve
kubectl run pod3 --image=nginx -n ivolve
```

### Verify Quota Enforcement
Try to create a 4th pod - it should fail:

```bash
kubectl run pod4 --image=nginx -n ivolve
# Expected error:
# The pod "pod4" is denied by the ResourceQuota "ivolveQuota" which has a total pod limit of 3.
```

### Create Pods Beyond Quota Limit (Should Fail)
```bash
kubectl run pod4 --image=nginx -n ivolve
# Error: The pod "pod4" is denied by the ResourceQuota "ivolveQuota" which has a total pod limit of 3.
```

## Namespace Best Practices

### When to Use Namespaces
1. **Multi-tenancy**: Isolate different teams or projects
2. **Resource Organization**: Group related resources together
3. **Access Control**: Apply different RBAC policies per namespace
4. **Quota Separation**: Apply different resource quotas per namespace

### Namespace Naming Conventions
- Use lowercase letters
- Use hyphens instead of spaces
- Prefix with project or team name (e.g., `ivolve-`, `dev-`, `prod-`)
- Avoid underscores and uppercase letters

## Cleanup

### Delete Namespace and All Resources
```bash
kubectl delete namespace ivolve
```

### Delete Only the Quota
```bash
kubectl delete resourcequota ivolveQuota -n ivolve
```

### List Resources in Namespace
```bash
kubectl get all -n ivolve
```