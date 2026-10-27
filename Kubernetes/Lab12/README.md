# Lab 12: ConfigMap and Secret

## Objective
Demonstrate how to use ConfigMaps and Secrets to manage configuration and sensitive data in Kubernetes.

## Setup

### Create a ConfigMap
Create a ConfigMap named `app-config` with configuration data:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: default
data:
  # Key-value pairs configuration
  DATABASE_URL: "postgresql://db:5432/mydb"
  LOG_LEVEL: "info"
  API_TIMEOUT: "30"
  ENVIRONMENT: "production"
```

Apply the ConfigMap:
```bash
kubectl apply -f configmap.yaml
```

### Verify ConfigMap
```bash
kubectl get configmap app-config -o yaml
kubectl describe configmap app-config
```

### Use ConfigMap in a Pod
Inject ConfigMap data into a pod:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
  - name: app-container
    image: nginx
    env:
      - name: DATABASE_URL
        valueFrom:
          configMapKeyRef:
            name: app-config
            key: DATABASE_URL
      - name: LOG_LEVEL
        valueFrom:
          configMapKeyRef:
            name: app-config
            key: LOG_LEVEL
```

### Create a Secret
Create a Secret named `app-secrets` with sensitive data:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: default
type: Opaque
data:
  # Values are base64 encoded
  DATABASE_PASSWORD: "c2FtcGxlQHByb2Zlcg=="  # base64 of "mysecretpassword"
  API_KEY: "YXBpX2FwaV9rZXk="  # base64 of "api-key-value"
```

Apply the Secret:
```bash
kubectl apply -f secret.yaml
```

### Verify Secret
```bash
kubectl get secret app-secrets
kubectl describe secret app-secrets
```

**Note**: Secrets data is base64 encoded, not encrypted. For production use, consider using Sealed Secrets or external secret managers.

### Use Secret in a Pod
Inject Secret data into a pod:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
  - name: app-container
    image: nginx
    env:
      - name: DATABASE_PASSWORD
        valueFrom:
          secretKeyRef:
            name: app-secrets
            key: DATABASE_PASSWORD
      - name: API_KEY
        valueFrom:
          secretKeyRef:
            name: app-secrets
            key: API_KEY
```

## ConfigMap vs Secret

| Feature | ConfigMap | Secret |
|---------|-----------|--------|
| **Purpose** | Non-sensitive configuration data | Sensitive data (passwords, keys, tokens) |
| **Encoding** | Stored in clear text (etcd) | Base64 encoded (not encrypted) |
| **Type** | Opaque, file, directory, etc. | Opaque, TLS, serviceAccount, docker-registry |
| **Security** | Anyone with cluster read access can view | Requires RBAC to view |
| **Use Case** | Application configuration, command-line arguments, environment variables | Database passwords, API keys, TLS certificates |

## Use Cases

### ConfigMap Use Cases
1. **Application Configuration**: Database URLs, API endpoints, feature flags
2. **Command-Line Arguments**: Pass configuration to application startup
3. **Pod Configuration**: Configure volume mounts, container arguments
4. **Static Data**: Configuration that changes infrequently

### Secret Use Cases
1. **Database Credentials**: Username and password combinations
2. **API Keys**: Authentication keys for third-party services
3. **TLS Certificates**: Certificate files for secure communication
3. **Docker Registry Credentials**: Registry authentication for pulling images
4. **OAuth Tokens**: Access and refresh tokens for authentication

## Cleanup

### Delete ConfigMap
```bash
kubectl delete configmap app-config
```

### Delete Secret
```bash
kubectl delete secret app-secrets
```

### Delete Pod
```bash
kubectl delete pod app-pod
```

## Verification Commands

### List All ConfigMaps
```bash
kubectl get configmaps -A
```

### List All Secrets
```bash
kubectl get secrets -A
```

### Describe a Specific ConfigMap
```bash
kubectl describe configmap app-config
```

### Describe a Specific Secret
```bash
kubectl describe secret app-secrets
```