# Lab 13: Persistent Storage Setup

## Overview
This lab demonstrates how to configure persistent storage in Kubernetes using PersistentVolumes (PV) and PersistentVolumeClaims (PVC).

## Components

### PersistentVolume (ivolvePV)
- **Name**: `ivolvePV`
- **Capacity**: 1Gi
- **Access Mode**: `ReadWriteMany` (allows multiple nodes to read/write)
- **Reclaim Policy**: `Retain` (data persists after PVC deletion)
- **Type**: `hostPath` at `/mnt/app-logs`
- **StorageClass**: None (static provisioning)

### PersistentVolumeClaim (ivolvePVC)
- **Name**: `ivolvePVC`
- **Namespace**: `ivolve`
- **Requested Storage**: 1Gi
- **Access Mode**: `ReadWriteMany`
- **Bound to**: `ivolvePV` via `volumeName`

## Deployment

```bash
# Apply the PV and PVC
kubectl apply -f pv-pvc.yaml

# Verify PV status
kubectl get pv ivolvePV

# Verify PVC status
kubectl get pvc ivolvePVC -n ivolve

# Check PV-PVC binding
kubectl describe pv ivolvePV
kubectl describe pvc ivolvePVC -n ivolve
```

## Verification

Expected output after successful deployment:

```
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM             STORAGECLASS   AGE
ivolvePV   1Gi        RWX            Retain           Bound    ivolve/ivolvePVC               5s

NAME        STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
ivolvePVC   Bound    ivolvePV   1Gi        RWX                           5s
```

## Notes
- The `hostPath` volume type is suitable for single-node clusters (like Docker Desktop)
- For production multi-node clusters, consider using network storage (NFS, CSI drivers)
- The `Retain` reclaim policy ensures data is not automatically deleted when the PVC is removed
- `ReadWriteMany` access mode allows the volume to be mounted by multiple pods simultaneously