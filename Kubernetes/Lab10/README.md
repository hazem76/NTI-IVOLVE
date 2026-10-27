# Lab 10: Node Isolation Using Taints and Labels

## Objective
Demonstrate how to control pod scheduling using taints and labels on Kubernetes nodes.

## Setup

### Taint a Worker Node
Taint the worker node with key `workload`, value `ivolveApp`, and effect `NoSchedule`:

```bash
kubectl taint nodes <node-name> workload=ivolveApp:NoSchedule
```

Example:
```bash
kubectl taint nodes node1.example.com workload=ivolveApp:NoSchedule
```

### Label the Same Worker Node
Label the worker node with `project=ivolve`:

```bash
kubectl label nodes <node-name> project=ivolve
```

Example:
```bash
kubectl label nodes node1.example.com project=ivolve
```

## Verification

### Describe Node
Describe the tainted and labeled node to verify the changes:

```bash
kubectl describe node <node-name>
```

Key sections to look for:
- **Taints**: Should show `workload=ivolveApp:NoSchedule`
- **Labels**: Should show `project=ivolve`

### Get Nodes with Labels
Get all nodes and display their labels:

```bash
kubectl get nodes --show-labels
```

Expected output format:
```
NAME           STATUS   ROLES    AGE   VERSION   LABELS
node1.example.com   Ready   <none>   1d    v1.29.0   project=ivolve,node-role.kubernetes.io/control-plane=master,,kubernetes.io/os=linux
```

## How Taints Work

### Taint Tolerations
Pods that want to schedule on the tainted node must have a matching toleration:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pod
spec:
  tolerations:
  - key: "workload"
    operator: "Equal"
    value: "ivolveApp"
    effect: "NoSchedule"
```

### Effect: NoSchedule
- Pods **will not** be scheduled on the tainted node **unless** they have a matching toleration.
- Existing pods on the node are **not** evicted.

## Use Cases

1. **Workload Isolation**: Separate different types of workloads on different nodes
2. **Resource Dedication**: Ensure specific workloads run on dedicated hardware
3. **Security**: Isolate sensitive workloads from general-purpose nodes
4. **Quality of Service**: Separate critical and best-effort workloads

## Cleanup

Remove the taint:
```bash
kubectl taint nodes <node-name> workload-
```

Remove the label:
```bash
kubectl label nodes <node-name> project-
```