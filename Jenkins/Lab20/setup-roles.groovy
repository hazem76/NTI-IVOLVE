// Jenkins Role-Based Strategy Setup Script
// This script configures Role-Based Authorization Strategy in Jenkins
// Run this via Jenkins Script Console or as an init script

import jenkins.model.*
import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy
import com.michelin.cio.hudson.plugins.rolestrategy.RoleGlob
import com.michelin.cio.hudson.plugins.rolestrategy.Role
import com.michelin.cio.hudson.plugins.rolestrategy.Permission

// Get Jenkins instance
Jenkins jenkins = Jenkins.get()

// ============================================
// 1. Create Jenkins Users: user1 and user2
// ============================================
def createUser(String username, String password) {
    def user = jenkins.getUser(username)
    if (user == null) {
        user = jenkins.createUser(username, password)
        println "Created user: $username"
    } else {
        println "User already exists: $username"
    }
    return user
}

def user1 = createUser("user1", "password123")
def user2 = createUser("user2", "password456")

// ============================================
// 2. Configure Role-Based Authorization Strategy
// ============================================
def strategy = new RoleBasedAuthorizationStrategy()

// Define roles
// Admin role - full access
def adminRole = new Role("admin")
adminRole.add(new Permission("Job", "Read"))
adminRole.add(new Permission("Job", "Configure"))
adminRole.add(new Permission("Job", "Create"))
adminRole.add(new Permission("Job", "Delete"))
adminRole.add(new Permission("Job", "Build"))
adminRole.add(new Permission("Job", "Cancel"))
adminRole.add(new Permission("Job", "Move"))
adminRole.add(new Permission("Job", "Workspace"))
adminRole.add(new Permission("Job", "Prompt"))
adminRole.add(new Permission("Run", "Read"))
adminRole.add(new Permission("Run", "Delete"))
adminRole.add(new Permission("Run", "Update"))
adminRole.add(new Permission("View", "Read"))
adminRole.add(new Permission("View", "Configure"))
adminRole.add(new Permission("View", "Create"))
adminRole.add(new Permission("View", "Delete"))
adminRole.add(new Permission("View", "Job"))
adminRole.add(new Permission("View", "JobConfigure"))
adminRole.add(new Permission("Overall", "RunScripts"))
adminRole.add(new Permission("Overall", "Administer"))
adminRole.add(new Permission("Overall", "Read"))

// Read-only role - limited access
def readonlyRole = new Role("read-only")
readonlyRole.add(new Permission("Job", "Read"))
readonlyRole.add(new Permission("Run", "Read"))
readonlyRole.add(new Permission("View", "Read"))
readonlyRole.add(new Permission("Overall", "Read"))

// Add roles to strategy
strategy.addRole(adminRole)
strategy.addRole(readonlyRole)

// ============================================
// 3. Assign Roles to Users
// ============================================
// Assign admin role to user1 (globally)
def adminGlob = new RoleGlob()
adminGlob.setPattern("user1")
adminGlob.addRole("admin")
strategy.addRoleGlob(adminGlob)

// Assign read-only role to user2 (globally)
def readonlyGlob = new RoleGlob()
readonlyGlob.setPattern("user2")
readonlyGlob.addRole("read-only")
strategy.addRoleGlob(readonlyGlob)

// ============================================
// 4. Apply the Strategy
// ============================================
jenkins.setAuthorizationStrategy(strategy)
jenkins.save()

println "========================================"
println "Jenkins Role-Based Authorization Setup Complete"
println "========================================"
println "Users created:"
println "  - user1: admin role (full access)"
println "  - user2: read-only role (view only)"
println ""
println "Validation steps:"
println "  1. Log in as user1 -> Should have full admin access"
println "  2. Log in as user2 -> Should have read-only access"
println "  3. user2 should NOT be able to create/delete jobs"