#!/bin/bash
# ============================================================
# Lab 9: Worker Node Join Command
# ============================================================
# This script generates and executes the kubeadm join command
# for worker nodes to join the cluster.
# ============================================================

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <control-plane-ip>"
  echo "Example: $0 192.168.1.100"
  exit 1
fi

CONTROL_PLANE_IP=$1

echo "🔗 Generating join command for worker node..."
JOIN_COMMAND=$(kubeadm token create --print-join-command --context kubernetes-control-plane)

# Replace the advertise address in the join command
sed "s/advertise-address=.*/advertise-address=${CONTROL_PLANE_IP}/" <<< "$JOIN_COMMAND"

echo "🏃 Running join command on worker node..."
# The actual join command would be run on the worker node:
# $JOIN_COMMAND