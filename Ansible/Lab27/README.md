# Lab 27: Securing Sensitive Data with Ansible Vault

## Overview
This lab demonstrates how to secure sensitive data using Ansible Vault. A MySQL role is created that installs MySQL server, creates a database, and configures a database user with vault-encrypted credentials.

## Directory Structure

```
Lab27/
├── inventory                    # Ansible inventory file
├── playbook.yml                 # Master playbook executing mysql role
├── README.md                    # This documentation
└── roles/
    └── mysql/
        ├── tasks/main.yml       # MySQL installation and configuration tasks
        ├── handlers/main.yml    # MySQL restart handler
        ├── vars/main.yml        # Vault-encrypted sensitive variables
        ├── defaults/main.yml    # Default variables (overridden by vault)
        ├── meta/main.yml        # Role metadata
        ├── files/               # Static files (empty)
        └── templates/           # Jinja2 templates (empty)
```

## MySQL Role

### Tasks (`roles/mysql/tasks/main.yml`)

| Task | Description |
|------|-------------|
| Install MySQL server | Install mysql-server and python3-pymysql |
| Start and enable MySQL | Ensure MySQL is running and enabled at boot |
| Create ivovle database | Create the ivovle database |
| Create ivolveuser user | Create database user with vault-encrypted password |
| Flush MySQL privileges | Apply privilege changes |

### Vault Variables (`roles/mysql/vars/main.yml`)

The `vars/main.yml` file contains sensitive data encrypted with Ansible Vault:

```yaml
mysql_root_password: "RootPass123!"
mysql_user_password: "Iv0lveUserPass!"
mysql_database: ivovle
mysql_user: ivolveuser
```

### Default Variables (`roles/mysql/defaults/main.yml`)

Default values that can be overridden by vault variables:

```yaml
mysql_root_password: "changeme"
mysql_user_password: "changeme"
mysql_database: ivovle
mysql_user: ivolveuser
mysql_version: "8.0"
```

## Ansible Vault Operations

### Encrypt the Vault File
```bash
# Encrypt vars/main.yml
ansible-vault encrypt roles/mysql/vars/main.yml

# Provide a vault password when prompted
# New Vault password: <enter-password>
# Confirm Vault password: <enter-password>
```

### Decrypt the Vault File
```bash
ansible-vault decrypt roles/mysql/vars/main.yml
```

### Edit Vault-Encrypted File
```bash
ansible-vault edit roles/mysql/vars/main.yml
```

### View Vault-Encrypted File
```bash
ansible-vault view roles/mysql/vars/main.yml
```

### Change Vault Password
```bash
ansible-vault rekey roles/mysql/vars/main.yml
```

### Create New Encrypted File
```bash
ansible-vault create roles/mysql/vars/new_secrets.yml
```

### Encrypt Multiple Files
```bash
ansible-vault encrypt roles/mysql/vars/main.yml roles/mysql/defaults/main.yml
```

## Execution

### Run Playbook with Vault
```bash
# Method 1: Prompt for vault password
ansible-playbook -i inventory --ask-vault-pass playbook.yml

# Method 2: Use vault password file
echo "your-vault-password" > ~/.vault_pass
chmod 600 ~/.vault_pass
ansible-playbook -i inventory --vault-password-file ~/.vault_pass playbook.yml

# Method 3: Use vault password from environment
export ANSIBLE_VAULT_PASSWORD_FILE=~/.vault_pass
ansible-playbook -i inventory playbook.yml
```

### Run in Check Mode
```bash
ansible-playbook -i inventory --ask-vault-pass --check playbook.yml
```

### Run with Verbose Output
```bash
ansible-playbook -i inventory --ask-vault-pass -v playbook.yml
```

## Database Validation

### Verify MySQL Installation
```bash
# On managed node
mysql --version
sudo systemctl status mysql
```

### Verify Database Creation
```bash
# Connect to MySQL
sudo mysql -u root -p

# In MySQL prompt:
SHOW DATABASES;
-- Should include: ivovle

USE ivovle;
SHOW TABLES;

# Verify user exists
SELECT User, Host FROM mysql.user WHERE User='ivolveuser';
```

### Verify User Privileges
```bash
# In MySQL prompt:
SHOW GRANTS FOR 'ivolveuser'@'%';
-- Should show ALL PRIVILEGES on ivovle.*
```

### Test User Connection
```bash
# Connect as ivolveuser
mysql -u ivolveuser -p'Iv0lveUserPass!' -h <node-ip> ivovle -e "SELECT 1;"

# Or from Ansible control node
mysql -u ivolveuser -p -h <node-ip> ivovle
```

## Security Best Practices

1. **Never commit vault passwords** to version control
2. **Store vault passwords** in a secure password manager or environment variable
3. **Use different vault passwords** for different environments (dev, staging, prod)
4. **Rotate vault passwords** regularly
5. **Limit access** to vault-encrypted files using file permissions
6. **Use Ansible Vault IDs** for multiple vault passwords:
   ```bash
   ansible-vault encrypt --vault-id dev@prompt roles/mysql/vars/main.yml
   ```

## Notes
- The `community.mysql` collection is required for mysql_db and mysql_user modules
- Install with: `ansible-galaxy collection install community.mysql`
- Vault variables in `vars/main.yml` override defaults in `defaults/main.yml`
- The database name `ivovle` and user `ivolveuser` are configured per lab requirements