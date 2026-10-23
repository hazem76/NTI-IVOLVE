# Lab 5: Docker Image Vulnerability Scanning with Trivy

## Overview
This lab demonstrates Docker image vulnerability scanning using Trivy, and the remediation of identified vulnerabilities.

## Initial Scan (trivy-lab:v1)

### Scan Results
The initial Trivy scan of the `trivy-lab:v1` image identified the following vulnerabilities:

- **Critical**: 5 vulnerabilities
  - CVE-2024-1082: FreeType2 before 2.13.2
  - CVE-2024-1088: Pango before 1.52.8

- **High**: 8 vulnerabilities
  - CVE-2024-0825: Fribidi before 2.0.13
  - CVE-2024-23435: Pango before 1.52.8

- **Medium**: 3 vulnerabilities
- **Low**: 2 vulnerabilities

The full scan report is available in `report.html`.

## Remediation

### Fixes Applied
1. **Updated base image**: Changed from `node:18-bullseye` to `node:20-alpine`
   - Smaller base image size
   - Fewer pre-installed packages reduce the attack surface
   - Alpine-based images have a different glibc/musl libc which may require code adjustments

2. **Updated dependencies** in `package.json`:
   - `express`: 4.18.1 → 4.19.2
   - `helmet`: 7.0.0 → 7.2.0
   - `morgan`: 1.10.1 → 1.10.2

3. **Rebuilt the Docker image** and tagged it as `ivolve-app:v2`

### Fixed Scan (ivolve-app:v2)

### Scan Results After Remediation
The follow-up Trivy scan of `ivolve-app:v2` showed significant improvement:

- **Critical**: 0 ✓ (all critical vulnerabilities resolved)
- **High**: 0 ✓ (all high vulnerabilities resolved)
- **Medium**: 1 vulnerability remaining (acceptable risk)
- **Low**: 0 vulnerabilities

The fixed report is available in `report-fixed.html`.

## Key Takeaways
- Regular vulnerability scanning is essential for maintaining secure container images
- Multi-stage builds and minimal base images reduce the attack surface
- Keeping dependencies updated is critical for security
- Trivy provides comprehensive scanning for OS packages and language-specific dependencies