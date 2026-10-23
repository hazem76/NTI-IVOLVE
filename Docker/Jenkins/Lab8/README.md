# Lab 8: Docker Compose Stack

## Overview
Docker Compose configuration for multi-container application stack.

## Services
- **app**: Nginx web server serving static content from `src/`
- **db**: PostgreSQL database for application persistence

## Quick Start
```bash
# Start the stack
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the stack
docker-compose down
```