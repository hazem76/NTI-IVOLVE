# Lab 20: Role-Based Authorization in Jenkins

## Overview
This lab implements Role-Based Authorization Strategy in Jenkins, creating two users with different access levels:
- **user1**: Admin role (full access to all Jenkins resources)
- **user2**: Read-only role (can view jobs and builds but cannot modify)

## Files

| File | Description |
|------|-------------|
| `setup-roles.groovy` | Groovy init script that creates users and configures roles |

## Setup Steps

### Prerequisites
1. Jenkins installed and running
2. **Role-Based Authorization Strategy plugin** installed
3. Admin access to Jenkins Script Console or ability to run init scripts

### Step 1: Install Required Plugin
```bash
# Install via Jenkins Plugin Manager
# Manage Jenkins -> Plugins -> Available plugins -> Role-Based Authorization Strategy
```

### Step 2: Run the Setup Script
Option A - Via Jenkins Script Console:
1. Go to **Manage Jenkins** -> **Script Console**
2. Copy the contents of `setup-roles.groovy`
3. Paste into the script console
4. Click **Run**

Option B - As Init Script:
1. Place `setup-roles.groovy` in `$JENKINS_HOME/init.groovy.d/`
2. Restart Jenkins

### Step 3: Configure Global Security
1. Go to **Manage Jenkins** -> **Configure Global Security**
2. Select **Role-Based Strategy** as Authorization
3. Save configuration

## Validation

### Test User1 (Admin)
1. Log in to Jenkins as `user1` / `password123`
2. Verify access to:
   - ✅ Create new jobs
   - ✅ Configure existing jobs
   - ✅ Delete jobs
   - ✅ Trigger builds
   - ✅ View all job details
   - ✅ Manage Jenkins (if Overall.Administer granted)

### Test User2 (Read-Only)
1. Log in to Jenkins as `user2` / `password456`
2. Verify access to:
   - ✅ View job list
   - ✅ View build details and console output
   - ❌ Create new jobs (denied)
   - ❌ Configure existing jobs (denied)
   - ❌ Delete jobs (denied)
   - ❌ Trigger builds (denied)

## Role Summary

| User | Role | Permissions |
|------|------|-------------|
| user1 | admin | Full read/write/create/delete on Jobs, Views, Runs, Overall |
| user2 | read-only | Read-only on Jobs, Views, Runs, Overall |

## Security Notes
- Passwords are set in plain text in this script for lab purposes
- In production, use environment variables or Jenkins credentials for passwords
- Consider adding more granular roles (e.g., developer, tester, operator)
- Roles can be scoped globally or per-item (job, folder, view)