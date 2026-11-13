# Lab 26: Structured Configuration Management with Ansible Roles

## Overview
This lab implements modular Ansible roles for structured configuration management. Three roles are created: docker, kubectl, and jenkins, orchestrated by a master site.yml playbook.

## Directory Structure

```
Lab26/
├── inventory                    # Ansible inventory file
├── site.yml                     # Master playbook orchestrating all roles
├── README.md                    # This documentation
└── roles/
    ├── docker/
    │   ├── tasks/main.yml       # Docker installation tasks
    │   ├── handlers/main.yml    # Docker restart handler
    │   ├── vars/main.yml        # Docker role variables
    │   ├── defaults/main.yml    # Docker role default variables
    │   ├── meta/main.yml        # Docker role metadata
    │   ├── files/               # Static files (empty)
    │   └── templates/           # Jinja2 templates (empty)
    ├── kubectl/
    │   ├── tasks/main.yml       # Kubectl installation tasks
    │   ├── handlers/main.yml    # Kubectl handlers (empty)
    │   ├── vars/main.yml        # Kubectl role variables
    │   ├── defaults/main.yml    # Kubectl role default variables
    │   ├── meta/main.yml        # Kubectl role metadata
    │   ├── files/               # Static files (empty)
    │   └── templates/           # Jinja2 templates (empty)
    └── jenkins/
        ├── tasks/main.yml       # Jenkins installation tasks
        ├── handlers/main.yml    # Jenkins restart handler
        ├── vars/main.yml        # Jenkins role variables
        ├── defaults/main.yml    # Jenkins role default variables
        ├── meta/main.yml        # Jenkins role metadata
        ├── files/               # Static files (empty)
        └── templates/           # Jinja2 templates (empty)
```

## Roles

### 1. Docker Role (`roles/docker`)

| File | Purpose |
|------|---------|
| `tasks/main.yml` | Install dependencies, add Docker repo, install Docker packages, start service, add users to docker group |
| `handlers/main.yml` | Restart Docker service on config changes |
| `vars/main.yml` | Define docker_users list |
| `defaults/main.yml` | Default docker_version and docker_users |
| `meta/main.yml` | Role metadata (author, license, platforms, tags) |

**Tasks:**
1. Install required apt dependencies
2. Add Docker GPG key
3. Add Docker apt repository
4. Install Docker packages (docker-ce, docker-ce-cli, containerd.io, docker-compose-plugin)
5. Start and enable Docker service
6. Add users to docker group

### 2. Kubectl Role (`roles/kubectl`)

| File | Purpose |
|------|---------|
| `tasks/main.yml` | Set kubectl version, download binary, verify installation |
| `handlers/main.yml` | No handlers defined |
| `vars/main.yml` | Define kubectl_version and install path |
| `defaults/main.yml` | Default kubectl version, path, architecture, OS |
| `meta/main.yml` | Role metadata |

**Tasks:**
1. Set kubectl version fact
2. Download kubectl binary from official Kubernetes release
3. Verify kubectl installation
4. Display installed version

### 3. Jenkins Role (`roles/jenkins`)

| File | Purpose |
|------|---------|
| `tasks/main.yml` | Install Java, add Jenkins repo, install Jenkins, configure port, start service, wait for readiness |
| `handlers/main.yml` | Restart Jenkins on port config change |
| `vars/main.yml` | Define jenkins_port |
| `defaults/main.yml` | Default jenkins_port and java package |
| `meta/main.yml` | Role metadata |

**Tasks:**
1. Install OpenJDK 17 JRE
2. Add Jenkins GPG key
3. Add Jenkins apt repository
4. Install Jenkins package
5. Configure Jenkins HTTP port
6. Start and enable Jenkins service
7. Wait for Jenkins to be ready on configured port

## Master Playbook: site.yml

```yaml
- name: Orchestrate Docker, Kubectl, and Jenkins Roles
  hosts: managed_nodes
  become: yes
  roles:
    - role: docker
      vars:
        docker_users:
          - vagrant
    - role: kubectl
    - role: jenkins
      vars:
        jenkins_port: 8080
```

## Execution

### Run the Master Playbook
```bash
ansible-playbook -i inventory site.yml
```

### Run Specific Role Only
```bash
# Run only docker role
ansible-playbook -i inventory site.yml --tags docker

# Run only kubectl role
ansible-playbook -i inventory site.yml --tags kubectl

# Run only jenkins role
ansible-playbook -i inventory site.yml --tags jenkins
```

### Run in Check Mode
```bash
ansible-playbook -i inventory --check site.yml
```

### Run with Verbose Output
```bash
ansible-playbook -i inventory -v site.yml
```

## Role Layout Best Practices

| Directory | Purpose |
|-----------|---------|
| `tasks/` | Main task list (main.yml required) |
| `handlers/` | Handlers triggered by task notifications |
| `vars/` | Role variables (higher priority than defaults) |
| `defaults/` | Default variables (lowest priority, easily overridden) |
| `meta/` | Role metadata (dependencies, Galaxy info) |
| `files/` | Static files for copy/template modules |
| `templates/` | Jinja2 templates for configuration files |

## Variable Precedence

1. `defaults/main.yml` - Lowest priority
2. `vars/main.yml` - Overrides defaults
3. Playbook-level vars - Overrides role vars
4. Inventory vars - Overrides playbook vars
5. Extra vars (`-e`) - Highest priority

## Verification

### Verify All Services
```bash
# Docker
docker --version
sudo systemctl status docker

# Kubectl
kubectl version --client

# Jenkins
sudo systemctl status jenkins
curl -I http://localhost:8080
```

## Notes
- All roles target Ubuntu/Debian-based systems
- The `become: yes` directive is set at the play level
- Role metadata follows Ansible Galaxy standards for potential sharing
- Each role is independently testable and reusable