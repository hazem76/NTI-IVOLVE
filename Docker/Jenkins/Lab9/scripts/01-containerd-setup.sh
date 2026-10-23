#!/bin/bash
# ============================================================
# Lab 9: containerd Installation and Configuration
# ============================================================
# This script installs and configures containerd as the container
# runtime with the systemd cgroup driver on all cluster nodes.
# ============================================================

set -e

echo "🔧 Updating system packages..."
apt-get update && apt-get upgrade -y

echo "📦 Installing containerd dependencies..."
apt-get install -y apt-transport-https curl gnupg2

echo "🔑 Adding Docker's GPG key..."
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -

echo "📋 Adding Docker repository..."
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

echo "📦 Installing containerd..."
apt-get update
apt-get install -y containerd.io

echo "⚙️ Configuring containerd with systemd cgroup driver..."
cat > /etc/containerd/config.toml << 'EOF'
[plugins."io.containerd.grpc.v1.cri".containerd]
  default_runtime_name = "runc"

[plugins."io.containerd.grpc.v1.cri".runtimes.runc]
  base_url = ""
  runtime_type = "runc"

[plugins."io.containerd.grpc.v1.cri".registry.configs]
  [plugins."io.containerd.grpc.v1.cri".registry.configs.default]
    endpoint = ["unix:///run/containerd/containerd.sock"]

[plugins."io.containerd.grpc.v1.cri".meta.containerd]

timeout = 2
shutdown_timeout = 3

[systemd]
  cgroup_controller = "cgroupfs"
  namesp_cgroup_controller = "cgroupfs"
EOF

echo "� restarting containerd..."
systemctl daemon-reload
systemctl containerd

echo "✅ containerd installed and configured with systemd cgroup driver!"