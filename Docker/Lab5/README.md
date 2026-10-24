# Lab 5: Docker Image Vulnerability Scanning with Trivy

## Overview
Lab 5 demonstrates Docker image vulnerability scanning using Trivy, a comprehensive container security scanner. This lab covers initial vulnerability scanning, identification of HIGH and CRITICAL issues, remediation through base image/dependency updates, and post-remediation scanning.

## Project Structure
```
Docker/Lab5/
├── src/                  ← Project files from TrivyImage
│   ├── app.js            ← Node.js application source
│   ├── Dockerfile        ← Node.js 18-bullseye Dockerfile
│   └── package.json      ← Node.js dependencies (express)
├── Dockerfile            ← Lab-level Dockerfile
├── report.html           ← Initial Trivy scan report (HIGH/CRITICAL)
├── report-fixed.html     ← Remediation scan report (vulnerabilities fixed)
├── trivy.yaml            ← Trivy scan configuration
└── README.md             ← Scan results, mitigation, and references
```

## Docker Image Build
```bash
# Build the Docker image
docker build -t trivy-lab:v1 .

# Verify the image was built
docker images | grep trivy-lab
```

## Initial Trivy Scan (Generate report.html)
```bash
# Scan the image for vulnerabilities, filter for HIGH and CRITICAL
trivy image --severity HIGH,CRITICAL trivy-lab:v1 > report.html

# View the scan results
cat report.html
```

## Remediation Phase
### Step 1: Update Base Image
```bash
# Update the base image from node:18-bullseye to a newer version
# Update package.json dependencies to latest secure versions
# Rebuild the Docker image
docker build -t ivolve-app:v2 .
```

### Step 2: Remediated Trivy Scan (Generate report-fixed.html)
```bash
# Scan the remediated image
trivy image --severity HIGH,CRITICAL ivolve-app:v2 > report-fixed.html

# View the improved scan results
cat report-fixed.html
```

## Verification
```bash
# Compare the two reports
echo "=== Initial Scan ==="
cat report.html
echo ""
echo "=== Remediated Scan ==="
cat report-fixed.html

# Verify image tags
docker images | grep -E "trivy-lab|ivolve-app"
```

## trivy.yaml Configuration
```yaml
schema_version: 2
severity_threshold: 'CRITICAL'
ignore_unfixed: false
context:
  readonly_filesystem: true
```