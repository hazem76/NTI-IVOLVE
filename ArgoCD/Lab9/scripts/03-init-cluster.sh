#!/bin/bash
# ============================================================
# Lab 9: Kubernetes Control Plane Initialization
# ============================================================
# This script initializes the control plane using kubeadm init,
# sets up local kubectl config, and deploys the Calico CNI
# plugin for pod networking.
# ============================================================

set -e

KUBERNETES_VERSION="1.29.0"
POD_CIDR="192.168.0.0/16"

echo "� initializing control plane with kubeadm..."
kubeadm init \
  --control-plane-endpoint "control-plane-node:6443" \
  --pod-network-cidr $POD_CIDR \
  --upload-certs

echo "📂 Setting up local kubectl config..."
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

echo "📦 Deploying Calico CNI plugin..."
kubectl apply -f https://docs.projectcalico.org/manifests/manifests.yaml

echo "⏳ Waiting for Calico to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=120s

echo "✅ Control plane initialized and Calico deployed!"
echo ""
echo "📝 Join worker nodes using:"
kubeadm token create --print-join-command