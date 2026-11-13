# Lab 28: Automated Host Discovery with Dynamic Inventory

## Overview
This lab demonstrates how to use AWS dynamic inventory with Ansible to automatically discover EC2 instances tagged with `server:ivolve`, instead of maintaining a static inventory file.

## Directory Structure

| Path | Description |
|------|-------------|
| `aws_ec2.yml` | AWS EC2 dynamic inventory configuration using amazon.aws.aws_ec2 plugin |
| `playbook.yml` | Test playbook targeting discovered hosts |
| `README.md` | This documentation |

## AWS Dynamic Inventory: aws_ec2.yml

### Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| Plugin | `amazon.aws.aws_ec2` | AWS EC2 inventory plugin |
| Regions | us-east-1, us-west-2, eu-west-1 | AWS regions to scan |
| Filters | instance-state-name=running, tag:server=ivolve | Only running instances with server:ivolve tag |
| Group by | instance_state, instance_type, region, tags | Dynamic grouping criteria |
| Compose ansible_host | public_ip_address | Connect via public IP |

### Groups Created

The plugin automatically creates groups such as:
- `tag_server_ivolve` - All instances with tag `server:ivolve`
- `instance_state_running` - All running instances
- `region_us-east-1` - All instances in us-east-1
- `instance_type_t2.micro` - All instances of type t2.micro

### Required AWS Tags

EC2 instances must have the following tags:
```
Key: server, Value: ivolve
Key: environment, Value: <optional>
Key: project, Value: <optional>
```

## Prerequisites

### 1. Install AWS CLI
```bash
# On Ubuntu/Debian
sudo apt update
sudo apt install -y awscli

# Configure AWS credentials
aws configure
# AWS Access Key ID: <your-access-key>
# AWS Secret Access Key: <your-secret-key>
# Default region: us-east-1
# Default output format: json
```

### 2. Install boto3 and botocore
```bash
pip3 install boto3 botocore
```

### 3. Install amazon.aws Collection
```bash
ansible-galaxy collection install amazon.aws
```

### 4. Configure AWS Credentials for Ansible
Option A - Environment variables:
```bash
export AWS_ACCESS_KEY_ID=<your-access-key>
export AWS_SECRET_ACCESS_KEY=<your-secret-key>
export AWS_DEFAULT_REGION=us-east-1
```

Option B - AWS credentials file (`~/.aws/credentials`):
```ini
[default]
aws_access_key_id = <your-access-key>
aws_secret_access_key = <your-secret-key>
region = us-east-1
```

## Execution

### List Discovered Hosts
```bash
# List all hosts discovered by dynamic inventory
ansible-inventory -i aws_ec2.yml --list

# List hosts in graph format
ansible-inventory -i aws_ec2.yml --graph

# Display host variables
ansible-inventory -i aws_ec2.yml --host <instance-id>
```

### Run Test Playbook
```bash
ansible-playbook -i aws_ec2.yml playbook.yml
```

### Run with Vault (if needed)
```bash
ansible-playbook -i aws_ec2.yml --ask-vault-pass playbook.yml
```

### Limit to Specific Group
```bash
ansible-playbook -i aws_ec2.yml --limit tag_server_ivolve playbook.yml
```

## Validation Steps

### 1. Verify Dynamic Inventory Works
```bash
ansible-inventory -i aws_ec2.yml --list | jq '.tag_server_ivolve.hosts'
```

Expected output shows discovered EC2 instances:
```json
["i-0123456789abcdef0", "i-0987654321fedcba9"]
```

### 2. Ping All Discovered Hosts
```bash
ansible all -i aws_ec2.yml -m ping
```

### 3. Run Full Playbook
```bash
ansible-playbook -i aws_ec2.yml playbook.yml
```

Expected tasks:
- Display discovered host information
- Ping connectivity test
- Check system uptime
- Install Docker (if Debian-based)
- Ensure Docker service is running

## AWS IAM Permissions

The AWS credentials used must have the following minimum permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances",
        "ec2:DescribeTags"
      ],
      "Resource": "*"
    }
  ]
}
```

## Notes
- Dynamic inventory is evaluated each time Ansible runs, so new instances are automatically discovered
- Instances are grouped by tags, enabling targeted playbook execution
- The `compose` section maps AWS attributes to Ansible connection variables
- For production, use AWS IAM roles instead of access keys when running on EC2
- Consider using `instance_states` filter to exclude terminated/stopped instances