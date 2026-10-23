# Lab 9: Kubeadm Cluster Deployment

## Overview
Kubeadm setup for on-premises Kubernetes cluster deployment.

## Prerequisites
- 3+ nodes (1 master, 2+ workers)
- Network connectivity between nodes
- Sufficient CPU and memory resources

## Setup Steps
1. Initialize master node: `kubeadm init`
2. Join worker nodes: `kubeadm join <token> <control-plane-ip>:<port>`
3. Install CNI plugin: `kubectl apply -f https://docs.projectcalico.org/manifests/manifests.yaml`
4. Verify cluster: `kubectl get nodes`

## Directory Structure
- `scripts/` - Deployment and management scripts

## Scripts
- `setup-master.sh` - Master node initialization
- `setup-worker.sh` - Worker node joining
- `deploy-network.sh` - Network plugin deployment