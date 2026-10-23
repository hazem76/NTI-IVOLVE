# Lab 9: Deploy Kubernetes Cluster Using Kubeadm

## Overview
Lab 9 demonstrates a complete on-premises Kubernetes cluster deployment using `kubeadm` as the bootstrapping tool. This lab covers the entire cluster lifecycle from container runtime installation to pod networking and node joining.

## Architecture
- **Control Plane Node**: Runs the API server, scheduler, and controller manager
- **Worker Nodes**: Run containerized applications (pods) managed by the control plane
- **Container Runtime**: containerd with systemd cgroup driver
- **Networking**: Calico CNI plugin for pod networking and network policies

## Prerequisites
- 3+ nodes (1 control plane + 2+ workers)
- Static IP addresses or resolvable hostnames for all nodes
- Network connectivity between all nodes (ports 6443, 179, 10250)
- Sufficient CPU and memory resources (minimum 2 CPU, 4GB RAM per node)
- Ubuntu 22.04 LTS or similar Linux distribution
- Sudo/root access on all nodes

## Installation Steps

### 1. Prepare All Nodes (Control Plane + Workers)
Run the following on every node in the cluster:

```bash
# Make scripts executable
chmod +x *.sh

# Install containerd with systemd cgroup driver
sudo ./01-containerd-setup.sh

# Install kubeadm, kubelet, and kubectl
sudo ./02-kubeadm-setup.sh

# Enable and start kubelet
sudo systemctl enable --now kubelet
```

### 2. Initialize Control Plane
Run only on the control plane node:

```bash
# Initialize the control plane
sudo ./03-init-cluster.sh

# Save the join command for worker nodes
sudo kubeadm token create --print-join-command --context kubernetes-control-plane > join-command.txt
```

### 3. Join Worker Nodes
Run on each worker node:

```bash
# Replace with your control plane node IP
sudo ./04-join-cluster.sh <CONTROL_PLANE_IP>

# Verify the join was successful
kubectl get nodes
```

### 4. Verify Cluster Status
Run on any node with kubectl configured:

```bash
# Check node status
kubectl get nodes

# Check all pods across namespaces
kubectl get pods -A

# Check cluster information
kubectl cluster-info
```

## Networking (Calico CNI)

The Calico CNI plugin is deployed automatically during initialization:

```bash
# Apply Calico manifest
kubectl apply -f https://docs.projectcalico.org/manifests/manifests.yaml

# Verify Calico is running
kubectl get po -n kube-system -l k8s-app=calico-node

# Check network connectivity
kubectl get nodes -o wide
```

## Cluster Verification Commands

### Node Status
```bash
kubectl get nodes
# Expected: All nodes show "Ready" status
```

### Pod Status (All Namespaces)
```bash
kubectl get pods -A
# Expected: All system pods (kube-system, calico-system) are Running
```

### Component Status
```bash
kubectl get componentstatuses
# Expected: controller-manager, scheduler, etcd are Healthy
```

### Service Exposure
```bash
# Check API server
kubectl get svc -n kube-system

# Check Calico endpoints
kubectl get svc -n calico-system
```

## Container Runtime Configuration

### containerd Configuration
The containerd config is located at `/etc/containerd/config.toml` and includes:
- `cgroup_controller = "cgroupfs"` for systemd compatibility
- `namesp_cgroup_controller = "cgroupfs"` for namespace cgroup handling
- Default runtime configured as `runc`

### Verifying containerd
```bash
# Check containerd status
systemctl status containerd

# Check running containers
crictl ps

# Verify cgroup driver
crictl info | grep -i "cgroup"
```

## Troubleshooting

### Common Issues

| Issue | Resolution |
|-------|------------|
| `kubectl get nodes` shows `NotReady` | Verify containerd is running, check kubelet status: `systemctl status kubelet` |
| Pods stuck in `Pending` state | Check node resources, verify network connectivity, ensure `pod-network-cidr` matches |
| `dial tcp: lookup control-plane: no such host` | Ensure control plane hostname/IP is reachable from worker nodes |
| Calico networking not functioning | Verify Calico pods are Running in kube-system, check IP pool configuration |

### Recovery Commands
```bash
# Reset a failed kubeadm init
sudo kubeadm reset

# Re-initialize control plane
sudo kubeadm init --pod-network-cidr=192.168.0.0/16

# Re-join worker nodes
sudo kubeadm join <control-plane-ip>:6443 --token <token> --discovery-token-ca-cert-hash <hash>
```

## Architecture Diagram

```
                   +----------------------+
                   |   Control Plane      |
                   |  (control-plane-node)|
                   +----------+-----------+
                                |
                                | kubeadm init + Calico
                                |
                    +--------v--------+
                    |   Worker Node 1 |
                    |   (worker-1)    |
                    +--------+--------+
                             |
             +-------------v-------------+
             |   kubelet + containerd    |
             |   +----------------------+ |
             |   |   Pods (your apps)   | |
             +-------------+-------------+
                        |   ^
                        |   | kubectl get pods -A
                        v   |
                +-----v-----+  |
                |   Services |  |
                +------------+  |
                                  
             +-------------v-------------+
             |   Worker Node 2           |
             |   (worker-2)              |
             +-------------+-------------+
                           |
           (kubeadm join command)
```

## Expected Results

- All nodes show `Ready` status via `kubectl get nodes`
- Calico CNI plugin pods are Running in `kube-system` namespace
- `kubectl get pods -A` shows all system pods healthy
- `kubectl cluster-info` displays correct API server endpoint
- Worker nodes successfully join the cluster and run pods