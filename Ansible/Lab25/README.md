# Lab 25: Automated Configuration Playbooks

## Overview
This lab implements a unified Ansible playbook that automates the installation of Docker, Kubectl, and Jenkins on managed nodes.

## Directory Structure

| Path | Description |
|------|-------------|
| `inventory` | Ansible inventory file defining managed nodes |
| `playbook.yml` | Main playbook installing Docker, Kubectl, and Jenkins |
| `README.md` | This documentation |

## Playbook: playbook.yml

### Plays

| Play | Target | Tasks |
|------|--------|-------|
| Install Docker, Kubectl, Jenkins | `managed_nodes` | 12 tasks + 2 handlers |

### Tasks Summary

1. **Update apt cache** - Refresh package repositories
2. **Install required dependencies** - Install apt-transport-https, curl, gnupg, etc.
3. **Add Docker GPG key** - Add Docker's official GPG key
4. **Add Docker repository** - Add Docker apt repository
5. **Install Docker** - Install docker-ce, docker-ce-cli, containerd.io, docker-compose-plugin
6. **Start and enable Docker service** - Ensure Docker is running and enabled at boot
7. **Add vagrant user to docker group** - Allow vagrant to run Docker without sudo
8. **Install Kubectl** - Download kubectl binary or install via snap as fallback
9. **Add Jenkins GPG key** - Add Jenkins repository GPG key
10. **Add Jenkins repository** - Add Jenkins apt repository
11. **Install Java** - Install OpenJDK 17 JRE (Jenkins dependency)
12. **Install Jenkins** - Install Jenkins package
13. **Configure Jenkins port** - Set custom HTTP port (default: 8080)
14. **Start and enable Jenkins service** - Start Jenkins and enable at boot
15. **Wait for Jenkins to be ready** - Wait until Jenkins port is accessible

### Handlers

| Handler | Triggered By | Action |
|---------|-------------|--------|
| restart docker | Docker config changes | Restart Docker service |
| restart jenkins | Jenkins port config change | Restart Jenkins service |

## Execution

### Run the Playbook
```bash
ansible-playbook -i inventory playbook.yml
```

### Run with Vault Password (if encrypted variables are used)
```bash
ansible-playbook -i inventory --ask-vault-pass playbook.yml
```

### Run in Check Mode (Dry Run)
```bash
ansible-playbook -i inventory --check playbook.yml
```

### Run with Verbose Output
```bash
ansible-playbook -i inventory -v playbook.yml
```

### Limit to Specific Hosts
```bash
ansible-playbook -i inventory --limit ivolve-node-1 playbook.yml
```

## Configuration Verification

### Verify Docker Installation
```bash
# On managed node
docker --version
docker info
sudo systemctl status docker

# Verify docker group membership
groups vagrant
```

### Verify Kubectl Installation
```bash
kubectl version --client
kubectl cluster-info
```

### Verify Jenkins Installation
```bash
# Check Jenkins service
sudo systemctl status jenkins

# Check Jenkins port
sudo netstat -tlnp | grep 8080

# Access Jenkins UI
# Open browser: http://<node-ip>:8080

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

## Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `docker_version` | 20.10.21 | Docker version to install |
| `kubectl_version` | v1.28.0 | Kubectl version to download |
| `jenkins_version` | 2.401.1 | Jenkins version to install |
| `jenkins_port` | 8080 | HTTP port for Jenkins |

## Notes
- Ensure managed nodes have passwordless sudo configured for the ansible user
- Replace IP addresses in inventory with your actual node IPs
- Jenkins initial admin password is at `/var/lib/jenkins/secrets/initialAdminPassword`
- For production, use Ansible Vault to encrypt sensitive variables
- The playbook uses `become: yes` for all tasks requiring root privileges