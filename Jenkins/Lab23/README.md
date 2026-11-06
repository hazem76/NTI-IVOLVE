# Lab 23: GitOps Workflow with Argo CD

## Overview
This lab implements a GitOps workflow using Argo CD. Instead of Jenkins applying manifests directly to Kubernetes, Jenkins commits updated deployment manifests to Git, and Argo CD automatically detects and syncs changes to the cluster.

## Directory Structure

| Path | Description |
|------|-------------|
| `argocd/ivovle-app.yaml` | Argo CD Application manifest targeting GitHub repo |
| `README.md` | This documentation |

## Argo CD Application: `ivovle`

| Property | Value |
|----------|-------|
| Name | `ivovle` |
| Namespace | `argocd` |
| Project | `default` |
| Repo URL | `https://github.com/hazem76/NTI-IVOLVE.git` |
| Target Revision | `main` |
| Manifest Path | `Jenkins/Lab21` |
| Destination Cluster | `https://kubernetes.default.svc` |
| Destination Namespace | `ivolve` |
| Sync Policy | Automated (prune: true, selfHeal: true) |

## GitOps Workflow

```
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Jenkins    │───>│    GitHub    │───>│   Argo CD        │───>│  Kubernetes     │
│  (Pipeline)  │    │   (Git)      │    │  (Application)   │    │   Cluster       │
│              │    │              │    │                  │    │                 │
│ Commits      │    │ Stores       │    │ Detects change   │    │ Applies manifest│
│ updated      │    │ deployment   │    │ via webhook/poll │    │ via kubectl     │
│ deployment   │    │ manifest     │    │                  │    │                 │
│ manifest     │    │              │    │                  │    │                 │
└──────────────┘    └──────────────┘    └──────────────────┘    └─────────────────┘
```

## Lab 22 Integration (GitOps Mode)

The `DeployOnK8s` shared library from Lab 22 supports GitOps mode:

```groovy
DeployOnK8s([
    gitOpsMode: true,
    gitRepo: 'https://github.com/hazem76/NTI-IVOLVE.git',
    gitBranch: 'main'
])
```

When `gitOpsMode=true`:
1. Jenkins updates `deployment.yaml` with new image tag
2. Commits and pushes to GitHub
3. Argo CD detects the Git change (webhook or polling)
4. Argo CD applies the updated manifest to Kubernetes

## Argo CD Installation

### Step 1: Install Argo CD
```bash
# Create Argo CD namespace
kubectl create namespace argocd

# Install Argo CD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Step 2: Expose Argo CD UI
```bash
# Port-forward to access UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Or create a LoadBalancer/Ingress for production
```

### Step 3: Get Admin Password
```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### Step 4: Login to Argo CD UI
- URL: `https://localhost:8080` (via port-forward)
- Username: `admin`
- Password: (from step 3)

## Application Registration

### Step 1: Apply the Application Manifest
```bash
kubectl apply -f Jenkins/Lab23/argocd/ivovle-app.yaml
```

### Step 2: Verify Application
```bash
kubectl get application ivovle -n argocd
kubectl describe application ivovle -n argocd
```

### Step 3: Sync the Application
In Argo CD UI:
1. Select application `ivovle`
2. Click **SYNC**
3. Enable **Auto-sync** with prune and self-heal

## Sync Validation

### Check Sync Status
```bash
# Via kubectl
kubectl get application ivovle -n argocd -o wide

# Via Argo CD CLI
argocd app get ivovle
```

Expected output:
```
Name:               ivovle
Project:            default
Server:             https://kubernetes.default.svc
Namespace:          ivolve
URL:                https://localhost:8080/applications/ivovle
Repo:               https://github.com/hazem76/NTI-IVOLVE.git
Target:             main
Path:               Jenkins/Lab21
Sync Policy:        Automated
Sync Status:        Synced
Health Status:      Healthy
```

### Verify Resources in Cluster
```bash
kubectl get deployment ivolve-app -n ivolve
kubectl get pods -n ivolve -l app=ivolve-app
kubectl rollout status deployment/ivolve-app -n ivolve
```

## Self-Heal & Prune Behavior

| Feature | Behavior |
|---------|----------|
| **Prune** | Resources removed from Git are also removed from cluster |
| **Self-Heal** | Manual changes to cluster resources are reverted to match Git |
| **Automated Sync** | Changes in Git are automatically applied within ~3 minutes |

## Required Jenkins Credentials

| Credential ID | Type | Description |
|---------------|------|-------------|
| `dockerhub-credentials` | Username/Password | Docker Hub for image push |
| `kubeconfig-credentials` | Secret File | Kubeconfig for cluster access (if not GitOps) |

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Application not found | Verify manifest applied with `kubectl apply -f` |
| Sync status Unknown | Check Argo CD controller pod: `kubectl get pods -n argocd` |
| Resources not created | Verify manifest path `Jenkins/Lab21` exists in repo |
| Self-heal reverting changes | Ensure all changes are committed to Git, not applied directly |
| ImagePullBackOff | Verify image tag was pushed to Docker Hub before commit |