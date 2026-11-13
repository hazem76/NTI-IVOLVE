# Lab 24: Initial Ansible Configuration & Ad-Hoc Execution

## Overview
This lab covers the initial setup of Ansible, including control node configuration, SSH key setup, inventory definition, and ad-hoc command execution to install Docker on managed nodes.

## Directory Structure

| Path | Description |
|------|-------------|
| `inventory` | Ansible inventory file defining managed nodes and groups |
| `README.md` | This documentation |

## Control Node Setup

### Step 1: Install Ansible
```bash
# On Ubuntu/Debian
sudo apt update
sudo apt install -y ansible

# On RHEL/CentOS
sudo yum install -y ansible

# Verify installation
ansible --version
```

### Step 2: Generate SSH Key Pair
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
```

This creates:
- Private key: `~/.ssh/id_rsa`
- Public key: `~/.ssh/id_rsa.pub`

### Step 3: Copy SSH Key to Managed Nodes
```bash
ssh-copy-id -i ~/.ssh/id_rsa.pub vagrant@192.168.1.10
ssh-copy-id -i ~/.ssh/id_rsa.pub vagrant@192.168.1.11
ssh-copy-id -i ~/.ssh/id_rsa.pub vagrant@192.168.1.12
```

Test SSH access:
```bash
ssh vagrant@192.168.1.10
```

### Step 4: Verify Inventory Connectivity
```bash
ansible all -i inventory -m ping
```

Expected output:
```
ivolve-node-1 | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
ivolve-node-2 | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
ivolve-node-3 | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
```

## Ad-Hoc Command Execution

### Install Docker on All Managed Nodes
```bash
ansible all -i inventory -m shell -a "sudo apt update && sudo apt install -y docker.io docker-compose && sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker $USER"
```

### Check Docker Version on All Nodes
```bash
ansible all -i inventory -m shell -a "docker --version"
```

### Check Docker Service Status
```bash
ansible all -i inventory -m shell -a "sudo systemctl status docker --no-pager"
```

### Run Ad-Hoc Command on Specific Group
```bash
ansible web_servers -i inventory -m shell -a "docker run hello-world"
ansible db_servers -i inventory -m shell -a "docker --version"
```

### Gather Facts from All Nodes
```bash
ansible all -i inventory -m setup
```

## Common Ad-Hoc Modules

| Module | Example Command | Description |
|--------|----------------|-------------|
| ping | `ansible all -i inventory -m ping` | Test connectivity |
| shell | `ansible all -i inventory -m shell -a "uptime"` | Run shell commands |
| command | `ansible all -i inventory -m command -a "whoami"` | Run commands (no shell) |
| apt | `ansible all -i inventory -m apt -a "name=docker.io state=present"` | Package management |
| service | `ansible all -i inventory -m service -a "name=docker state=started"` | Service management |
| setup | `ansible all -i inventory -m setup` | Gather system facts |
| copy | `ansible all -i inventory -m copy -a "src=file.txt dest=/tmp/"` | Copy files |

## Notes
- Replace IP addresses and usernames with your actual managed node details
- Ensure passwordless sudo is configured on managed nodes for Docker installation
- For production, use Ansible Vault to protect sensitive inventory variables
- The `ansible_ssh_common_args` variable disables host key checking for lab purposes