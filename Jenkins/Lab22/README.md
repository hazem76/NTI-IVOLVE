# Lab 22: Shared Libraries & Jenkins Agents

## Overview
This lab demonstrates Jenkins Shared Libraries and agent-based distributed builds. It implements a modular CI/CD pipeline using shared library global variables and a dedicated Jenkins agent node labeled `ivolve-slave`.

## Directory Structure

| Path | Description |
|------|-------------|
| `vars/BuildApp.groovy` | Shared library global variable: builds app and runs unit tests |
| `vars/BuildImage.groovy` | Shared library global variable: builds Docker image, Trivy scan, push, cleanup |
| `vars/DeployOnK8s.groovy` | Shared library global variable: updates manifest and deploys to K8s or GitOps |
| `src/BuildApp.groovy` | Shared library source class for BuildApp |
| `src/BuildImage.groovy` | Shared library source class for BuildImage |
| `src/DeployOnK8s.groovy` | Shared library source class for DeployOnK8s |
| `src/app.js` | Sample Node.js application |
| `src/package.json` | npm dependencies |
| `Dockerfile` | Production Docker build |
| `Jenkinsfile` | Pipeline using shared libraries and `ivolve-slave` agent |
| `README.md` | This documentation |

## Shared Library Configuration

### 1. Add Shared Library to Jenkins
1. Go to **Manage Jenkins** -> **Configure System**
2. Find **Global Pipeline Libraries** section
3. Click **Add** -> **Git**
4. Configure:
   - **Name**: `ivolve-shared-library`
   - **Repository URL**: `https://github.com/hazem76/NTI-IVOLVE.git`
   - **Default version**: `main`
   - **Retrieve library from**: `Jenkins/Lab22`

### 2. Library Structure
```
vars/
  BuildApp.groovy      # Global variable entry point
  BuildImage.groovy    # Global variable entry point
  DeployOnK8s.groovy   # Global variable entry point
src/
  com/ivolve/lab22/
    BuildApp.groovy    # Implementation class
    BuildImage.groovy  # Implementation class
    DeployOnK8s.groovy # Implementation class
```

### 3. Using Shared Library in Jenkinsfile
```groovy
@Library('ivolve-shared-library') _

pipeline {
    agent { label 'ivolve-slave' }
    
    stages {
        stage('Build App') {
            steps {
                BuildApp()
            }
        }
        stage('Build Image') {
            steps {
                script {
                    def image = BuildImage([...])
                }
            }
        }
    }
}
```

## Jenkins Agent Setup (`ivolve-slave`)

### Prerequisites
- A separate machine/VM to act as Jenkins agent
- Java JDK 11+ installed
- Docker installed and running
- kubectl configured
- Git installed

### Step 1: Create Agent Node in Jenkins
1. Go to **Manage Jenkins** -> **Nodes**
2. Click **New Node**
3. Name: `ivolve-slave`
4. Type: **Permanent Agent**
5. Click **Create**

### Step 2: Configure Agent
- **Remote root directory**: `/home/jenkins/agent` (or similar)
- **Labels**: `ivolve-slave`
- **Launch method**: Launch agent via Java Web Start or SSH
- **Save**

### Step 3: Launch Agent
1. Click on the `ivolve-slave` node
2. Download the `agent.jar` file
3. Run on the agent machine:
```bash
java -jar agent.jar -jnlpUrl <JENKINS_URL>/computer/ivolve-slave/slave-agent.jnlp
```

Or via SSH:
```bash
ssh jenkins@<agent-host>
```

### Step 4: Verify Agent
- Node status should show **Online**
- Label `ivolve-slave` is available for pipeline scheduling

## Pipeline Stages

| Stage | Shared Library | Description |
|-------|---------------|-------------|
| Checkout | - | SCM checkout |
| Build App | `BuildApp()` | npm install + test |
| Build Image | `BuildImage()` | Docker build, Trivy scan, push, cleanup |
| Deploy to K8s | `DeployOnK8s()` | Update manifest + apply or GitOps commit |

## GitOps Mode

When `GITOPS_MODE=true`:
1. `DeployOnK8s` updates `deployment.yaml` with new image tag
2. Commits and pushes changes to Git repository
3. Argo CD detects the change and syncs automatically (see Lab 23)

When `GITOPS_MODE=false`:
1. `DeployOnK8s` applies manifest directly via `kubectl apply`

## Required Jenkins Credentials

| Credential ID | Type | Description |
|---------------|------|-------------|
| `dockerhub-credentials` | Username/Password | Docker Hub credentials |
| `kubeconfig-credentials` | Secret File | Kubernetes kubeconfig file |

## Validation

1. Run the pipeline
2. Verify all stages complete successfully
3. Check Docker Hub for the pushed image
4. Verify Kubernetes deployment:
   ```bash
   kubectl get pods -n ivolve -l app=ivolve-app
   kubectl rollout status deployment/ivolve-app -n ivolve
   ```
5. In GitOps mode, verify commit appears in GitHub and Argo CD syncs