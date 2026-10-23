#!/bin/bash
# ============================================================
# Lab 9: kubeadm, kubelet, kubectl Installation
# ============================================================
# This script installs kubeadm, kubelet, and kubectl on all
# cluster nodes and disables the swap file required for kubeadm.
# ============================================================

set -e

echo "🔧 Updating system packages..."
apt-get update && apt-get upgrade -y

echo "📦 Installing Kubernetes dependencies..."
apt-get install -y apt-transport-https curl gpg

echo "🔑 Adding Kubernetes GPG key..."
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/rubygems/releases.gpg | gpg --dearmor -o /etc/apt/keyrings/kubernetes-archive-keyring.gpg

echo "📋 Adding Kubernetes repository..."
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-archive-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/apt/ /' | tee -a /etc/apt/sources.list.d/kubernetes.list

echo "📦 Installing kubeadm, kubelet, and kubectl..."
apt-get update
apt-get install -y kubelet kubeadm kubectl

echo "⚙️ Marking packages on hold to prevent auto-update..."
apt-mark hold kubelet kubeadm kubectl

echo "🔧 Disabling swap (required for kubeadm)..."
swapoff -a
sed -i '/swap/d /etc/fstab'

echo "✅ kubeadm, kubelet, and kubectl installed successfully!"
echo "📝 Remember to enable kubelet: systemctl enable --now kubelet"