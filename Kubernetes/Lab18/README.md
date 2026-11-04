# Lab 18: Node-Wide Pod Management with DaemonSet

## Overview
This lab deploys a complete monitoring stack in the `monitoring` namespace, including Prometheus, AlertManager, Grafana, and a Node Exporter DaemonSet that runs on every node in the cluster.

## Components

### Namespace
- **Name**: `monitoring`
- **Purpose**: Isolates all monitoring infrastructure from application workloads

### Prometheus
- **Deployment**: `prometheus` (1 replica)
- **Image**: `prom/prometheus:latest`
- **Port**: 9090
- **Service**: `prometheus` (ClusterIP)
- **ConfigMap**: `prometheus-config` with scrape configs for:
  - Prometheus itself
  - Node Exporter pods
  - Kubernetes pods with Prometheus annotations

### AlertManager
- **Deployment**: `alertmanager` (1 replica)
- **Image**: `prom/alertmanager:latest`
- **Port**: 9093
- **Service**: `alertmanager` (ClusterIP)
- **ConfigMap**: `alertmanager-config` with routing rules

### Node Exporter DaemonSet
- **DaemonSet**: `node-exporter`
- **Image**: `prom/node-exporter:latest`
- **Port**: 9100
- **Service**: `node-exporter` (ClusterIP)
- **Purpose**: Runs on every node to expose host-level metrics (CPU, memory, disk, network)
- **Host mounts**: `/proc`, `/sys`, `/` (read-only) for host metrics collection

### Grafana
- **Deployment**: `grafana` (1 replica)
- **Image**: `grafana/grafana:latest`
- **Port**: 3000
- **Service**: `grafana` (NodePort on port 30000)
- **Admin credentials**: admin / admin
- **ConfigMap**: `grafana-dashboard` with Kubernetes cluster dashboard

## Deployment

```bash
# Apply all manifests in order
kubectl apply -f manifests/namespace.yaml
kubectl apply -f manifests/configmap-prometheus.yaml
kubectl apply -f manifests/configmap-alertmanager.yaml
kubectl apply -f manifests/configmap-grafana.yaml
kubectl apply -f manifests/deployment-prometheus.yaml
kubectl apply -f manifests/service-prometheus.yaml
kubectl apply -f manifests/deployment-alertmanager.yaml
kubectl apply -f manifests/service-alertmanager.yaml
kubectl apply -f manifests/daemonset-node-exporter.yaml
kubectl apply -f manifests/service-node-exporter.yaml
kubectl apply -f manifests/deployment-grafana.yaml
kubectl apply -f manifests/service-grafana.yaml

# Or apply all at once
kubectl apply -f manifests/
```

## Validation

### 1. Verify Node Exporter DaemonSet runs on all nodes
```bash
kubectl get daemonset node-exporter -n monitoring
kubectl get pods -n monitoring -l app=node-exporter -o wide
```

Expected: One pod per node, all in Running state
```
NAME             READY   STATUS    RESTARTS   AGE   NODE
node-exporter-xxx   1/1     Running   0          5m    node-1
node-exporter-yyy   1/1     Running   0          5m    node-2
```

### 2. Verify metrics exposure on :9100/metrics
```bash
# From any node-exporter pod
kubectl exec -n monitoring daemonset/node-exporter -- curl -s http://localhost:9100/metrics | head -20
```

Expected: Prometheus-format metrics including `node_cpu_seconds_total`, `node_memory_MemTotal_bytes`, etc.

### 3. Verify Prometheus scraping targets
```bash
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Then visit: http://localhost:9090/targets
```

Expected: All targets (prometheus, node-exporter, kubernetes-pods) showing as UP

### 4. Verify Grafana dashboard setup
```bash
kubectl port-forward -n monitoring svc/grafana 3000:3000
# Then visit: http://localhost:3000
# Login: admin / admin
```

Expected: Kubernetes Cluster Monitoring dashboard with CPU and Memory panels

## Notes
- DaemonSet ensures exactly one node-exporter pod per node, even when new nodes are added
- Node exporter uses hostPath mounts to access host `/proc`, `/sys`, and root filesystem
- Grafana uses NodePort (30000) for easy access from outside the cluster
- All resources are in the `monitoring` namespace for clean separation