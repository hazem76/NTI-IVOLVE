# Lab 21: Jenkins Pipeline for Application Deployment

## Overview
This lab implements a complete CI/CD pipeline using Jenkins Declarative Pipeline that:
1. Builds a Node.js application
2. Builds a Docker image
3. Scans the image with Trivy for vulnerabilities
4. Pushes the image to Docker Hub
5. Cleans up local images
6. Updates Kubernetes deployment manifest
7. Deploys to Kubernetes cluster

## Directory Structure

| Path | Description |
|------|-------------|
| `src/` | Sample Node.js application |
| `Dockerfile` | Production-ready multi-stage Docker build |
| `deployment.yaml` | Kubernetes Deployment manifest |
| `Jenkinsfile` | Declarative Jenkins pipeline |
| `README.md` | This documentation |

## Prerequisites

### Jenkins Configuration

#### Required Credentials
Configure the following credentials in Jenkins (Manage Jenkins -> Credentials):

| Credential ID | Type | Description |
|---------------|------|-------------|
| `dockerhub-credentials` | Username with password | Docker Hub username and password/token |
| `kubeconfig-credentials` | Secret file | Kubernetes kubeconfig file for cluster access |

#### Required Plugins
- Pipeline
- Docker Pipeline
- Kubernetes CLI
- Credentials Binding
- SSH Agent (optional)

### Application
- Node.js application in `src/` directory
- Docker available on Jenkins agent
- kubectl configured on Jenkins agent
- Access to Docker Hub registry
- Access to Kubernetes cluster

## Pipeline Stages

| Stage | Description |
|-------|-------------|
| **Checkout** | Checks out source code from SCM |
| **Build App** | Installs npm dependencies and runs tests |
| **Build Docker Image** | Builds Docker image tagged as `ivolve-app:${BUILD_NUMBER}` |
| **Scan Docker Image** | Runs Trivy vulnerability scan (HIGH, CRITICAL) |
| **Push Image** | Authenticates with Docker Hub and pushes image |
| **Delete Local Image** | Removes local Docker image to free storage |
| **Update Deployment Manifest** | Updates image tag in `deployment.yaml` using sed |
| **Deploy to Kubernetes** | Applies updated manifest via `kubectl apply` |

## Execution Steps

### 1. Create Jenkins Pipeline Job
1. Go to Jenkins Dashboard -> **New Item**
2. Enter job name: `ivolve-app-pipeline`
3. Select **Pipeline** -> **OK**
4. In Pipeline section:
   - Definition: **Pipeline script from SCM**
   - SCM: Select your Git repository
   - Script Path: `Jenkins/Lab21/Jenkinsfile`
5. Save

### 2. Run the Pipeline
1. Click **Build Now** on the pipeline job
2. Monitor each stage in the build logs
3. Verify deployment:
   ```bash
   kubectl get pods -n ivolve -l app=ivolve-app
   kubectl get deployment ivolve-app -n ivolve
   ```

### 3. Verify Deployment
```bash
# Check pods are running
kubectl get pods -n ivolve

# Check deployment status
kubectl rollout status deployment/ivolve-app -n ivolve

# Port-forward for local testing
kubectl port-forward -n ivolve svc/ivolve-app-svc 3000:3000
# Then: curl http://localhost:3000/health
```

## Post Actions

| Condition | Action |
|-----------|--------|
| `always` | Clean workspace, print completion message |
| `success` | Print success message with deployed image tag |
| `failure` | Print failure message with build URL for debugging |

## Docker Image Tagging

Images are tagged using the Jenkins build number:
```
hazem7777/nodejs-docker-exercise:${BUILD_NUMBER}
```

Example: `hazem7777/nodejs-docker-exercise:42`

## Security Notes

- Docker Hub credentials are injected via `withCredentials` (never logged)
- Kubeconfig is injected as a secret file (never logged)
- Trivy scan runs with `--exit-code 0` to not fail pipeline on vulnerabilities (adjust for production)
- Consider adding image digest verification for production deployments

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Docker build fails | Ensure Docker daemon is running on Jenkins agent |
| Trivy scan fails | Check Docker socket mount permissions |
| Docker push fails | Verify dockerhub-credentials are correct |
| kubectl apply fails | Verify kubeconfig-credentials path and cluster access |
| ImagePullBackOff | Check image tag matches what was pushed to Docker Hub |