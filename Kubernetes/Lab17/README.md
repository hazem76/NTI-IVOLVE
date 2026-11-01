# Lab 17: Control Pod-to-Pod Traffic via Network Policy

## Overview
This lab implements a Kubernetes NetworkPolicy to restrict ingress traffic to the MySQL database (StatefulSet from Lab 14), allowing connections only from the application pods (Deployment from Lab 15) on port 3306.

## NetworkPolicy (ivolveNP)

### Specification
- **Name**: `ivolveNP`
- **Namespace**: `ivolve`
- **Target Pods**: `app: ivolveDB` (MySQL StatefulSet pods)
- **Policy Type**: `Ingress` (controls incoming traffic)
- **Ingress Rules**:
  - **From**: Pods with label `app: ivolveApp` (Node.js application pods)
  - **Ports**: TCP port 3306 (MySQL)

### Behavior
- **Allowed**: Traffic from `ivolveApp` pods to `ivolveDB` pods on port 3306
- **Denied**: All other ingress traffic to `ivolveDB` pods (including from other namespaces, external sources, or other pods in the same namespace)
- **Egress**: Unrestricted (no egress rules specified)

## Deployment

```bash
# Apply the NetworkPolicy
kubectl apply -f networkpolicy.yaml

# Verify NetworkPolicy
kubectl get networkpolicy ivolveNP -n ivolve
kubectl describe networkpolicy ivolveNP -n ivolve
```

## Verification

### 1. Check NetworkPolicy Status
```bash
kubectl get networkpolicy -n ivolve
```

Expected output:
```
NAME       POD-SELECTOR     AGE
ivolveNP   app=ivolveDB     30s
```

### 2. Test Allowed Connection (from ivolveApp pod)
```bash
# From an application pod - should succeed
kubectl exec -it -n ivolve deploy/ivolveApp -- \
  mysql -h ivolveDBSvc -u ivolve -ppassword -e "SELECT 1;"

# Or using netcat/telnet
kubectl exec -it -n ivolve deploy/ivolveApp -- \
  nc -zv ivolveDBSvc 3306
```

Expected: Connection succeeds

### 3. Test Denied Connection (from unauthorized pod)
```bash
# Create a test pod without the required label
kubectl run -it --rm test-pod --image=busybox --restart=Never -n ivolve -- \
  nc -zv ivolveDBSvc 3306
```

Expected: Connection timeout/refused (blocked by NetworkPolicy)

### 4. Test from Different Namespace
```bash
# Create test pod in default namespace
kubectl run -it --rm test-pod --image=busybox --restart=Never -- \
  nc -zv ivolveDBSvc.ivolve.svc.cluster.local 3306
```

Expected: Connection blocked (no matching podSelector in ingress rule)

### 5. Verify NetworkPolicy Details
```bash
kubectl describe networkpolicy ivolveNP -n ivolve
```

Expected output:
```
Name:         ivolveNP
Namespace:    ivolve
Created on:   2026-11-01 ...
Labels:       <none>
Annotations:  <none>
Spec:
  PodSelector:     app=ivolveDB
  Allowing ingress traffic:
    To Port: 3306/TCP
    From:
      PodSelector: app=ivolveApp
  Not affecting egress traffic
  Policy Types: Ingress
```

## Testing Strategy

### Pre-Policy Baseline (Optional)
Before applying the policy, verify unrestricted access:
```bash
# Should work before policy
kubectl exec -it -n ivolve deploy/ivolveApp -- nc -zv ivolveDBSvc 3306
```

### Post-Policy Verification
After applying the policy, verify:
1. ✅ Application pods can still connect to MySQL
2. ❌ Unlabeled pods in same namespace cannot connect
3. ❌ Pods from other namespaces cannot connect
4. ❌ External access is blocked

### Cleanup Test Resources
```bash
# Remove any test pods created during verification
kubectl delete pod test-pod -n ivolve --ignore-not-found=true
kubectl delete pod test-pod --ignore-not-found=true
```

## Notes
- NetworkPolicy requires a CNI plugin that supports NetworkPolicy (e.g., Calico, Cilium, Weave)
- Docker Desktop's built-in Kubernetes uses a CNI that supports NetworkPolicy
- The policy is namespace-scoped; it only affects traffic within the `ivolve` namespace
- Egress traffic from MySQL pods is unrestricted (no egress rules)
- To allow monitoring/debugging tools, add additional `podSelector` entries to the `from` clause
- NetworkPolicy is additive: if multiple policies select the same pod, the union of allowed ingress is permitted