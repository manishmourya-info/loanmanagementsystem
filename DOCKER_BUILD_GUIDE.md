# Docker Build Guide - JIB Configuration

## Issue Fixed ✅

**Problem**: Docker image reference contained uppercase letters, violating Docker naming conventions
```
Invalid: docker.io/${DOCKER_HUB_USER}/loan-management-system:1.0.0
Error: "slash-separated name components cannot have uppercase letters"
```

**Solution**: Updated Maven configuration to use `${docker.hub.user}` property which automatically uses `${env.DOCKER_HUB_USER}` in lowercase

---

## Prerequisites

### 1. Set Environment Variables

**Option A: PowerShell (Windows)**
```powershell
# Set environment variables (must be lowercase for Docker)
$env:DOCKER_HUB_USER = "yourdockerusername"
$env:DOCKER_HUB_TOKEN = "yourtoken"

# Verify they're set
Write-Host "User: $env:DOCKER_HUB_USER"
Write-Host "Token: $env:DOCKER_HUB_TOKEN"
```

**Option B: System Environment Variables**
1. Open Settings → System → Environment Variables
2. Click "New" and add:
   - Name: `DOCKER_HUB_USER`  Value: `yourdockerusername` (lowercase)
   - Name: `DOCKER_HUB_TOKEN` Value: `your-token-here`
3. Restart terminal/IDE

**Option C: .env file (Development Only)**
Create `.env` file in project root:
```
DOCKER_HUB_USER=yourdockerusername
DOCKER_HUB_TOKEN=your-token-here
```

### 2. Verify Docker Hub Access
```powershell
# Login to Docker Hub
docker login

# Verify credentials
docker info

# Test image pull
docker pull openjdk:17-jdk-slim
```

### 3. Verify Maven Configuration
```powershell
# Check JIB configuration in pom.xml
Select-String -Path "pom.xml" -Pattern "docker.hub.user|jib-maven-plugin"
```

---

## Build Instructions

### Build 1: Compile Only (No Docker)
```powershell
cd d:\POC\loan-management-system
mvn clean compile
```

### Build 2: Create Local Docker Image
```powershell
# Build JAR and create local Docker image
mvn clean package -DskipTests jib:dockerBuild

# Verify image was created
docker images | grep "loan-management-system"
```

### Build 3: Push to Docker Hub
```powershell
# Build JAR and push to Docker Hub registry
# Requires DOCKER_HUB_TOKEN with write permissions
mvn clean package -DskipTests jib:build
```

### Build 4: Skip Docker Plugin (if no credentials)
```powershell
# Build without JIB Docker plugin
mvn clean package -DskipTests -DskipJib=true

# Or skip plugin execution:
mvn clean package -DskipTests -DskipDockerBuild=true
```

---

## Configuration Details

### Maven Properties (pom.xml)
```xml
<properties>
    <docker.hub.user>${env.DOCKER_HUB_USER}</docker.hub.user>
</properties>
```

### JIB Plugin Configuration
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <from>
            <image>openjdk:17-jdk-slim</image>
        </from>
        <to>
            <image>docker.io/${docker.hub.user}/loan-management-system:${project.version}</image>
            <auth>
                <username>${env.DOCKER_HUB_USER}</username>
                <password>${env.DOCKER_HUB_TOKEN}</password>
            </auth>
        </to>
        <container>
            <jvmFlags>
                <jvmFlag>-XX:+UseG1GC</jvmFlag>
                <jvmFlag>-XX:MaxRAMPercentage=75</jvmFlag>
                <jvmFlag>-XX:+UseStringDeduplication</jvmFlag>
            </jvmFlags>
        </container>
    </configuration>
</plugin>
```

---

## Docker Image Details

### Image Reference
- **Registry**: `docker.io` (Docker Hub)
- **Namespace**: `${docker.hub.user}` (your username, lowercase)
- **Repository**: `loan-management-system`
- **Tag**: `1.0.0` (from pom.xml `<version>`)
- **Final**: `docker.io/yourdockerusername/loan-management-system:1.0.0`

### Base Image
- **From**: `openjdk:17-jdk-slim`
- **Size**: ~340MB (optimized slim variant)
- **JVM**: OpenJDK 17 LTS

### JVM Flags
- `-XX:+UseG1GC` - Garbage First Collector for reduced latency
- `-XX:MaxRAMPercentage=75` - Use 75% of container RAM
- `-XX:+UseStringDeduplication` - Reduce memory for duplicate strings
- `-XX:+UseStringCache` - Cache frequently used strings

---

## Troubleshooting

### Error: "Invalid image reference ... uppercase letters"

**Cause**: `DOCKER_HUB_USER` environment variable contains uppercase letters

**Solution**:
```powershell
# Check current value
Write-Host $env:DOCKER_HUB_USER

# Convert to lowercase
$env:DOCKER_HUB_USER = $env:DOCKER_HUB_USER.ToLower()

# Verify
Write-Host $env:DOCKER_HUB_USER

# Retry build
mvn clean package -DskipTests jib:dockerBuild
```

### Error: "Unable to authenticate with registry"

**Cause**: Missing or invalid credentials

**Solution**:
```powershell
# 1. Verify credentials
$env:DOCKER_HUB_USER
$env:DOCKER_HUB_TOKEN

# 2. Test Docker Hub login
docker login -u $env:DOCKER_HUB_USER -p $env:DOCKER_HUB_TOKEN

# 3. If successful, retry JIB build
mvn clean package -DskipTests jib:build
```

### Error: "Repository does not exist or permission denied"

**Cause**: Credentials lack push permissions or repository doesn't exist

**Solution**:
```powershell
# 1. Create repository on Docker Hub (manual step)
# Visit https://hub.docker.com/repositories and create:
#   Repository Name: loan-management-system
#   Visibility: Private or Public
#   Description: Consumer Finance Loan Management System

# 2. Verify token has appropriate permissions:
#   - Account Settings → Security → Personal Access Tokens
#   - Ensure token has "Read & Write" permissions

# 3. Retry build
mvn clean package -DskipTests jib:build
```

### Error: "Layers built in ... (build details) but failed to push"

**Cause**: Image built locally but failed to push to registry

**Solution**:
```powershell
# 1. Check Docker Hub connectivity
Test-NetConnection hub.docker.com -Port 443

# 2. Verify Docker daemon is running
docker info

# 3. Re-authenticate with Docker Hub
docker logout
docker login

# 4. Retry with verbose output
mvn clean package -DskipTests jib:build -X 2>&1 | Tee-Object -FilePath build.log
```

---

## Next Steps

### 1. Local Testing
```powershell
# Run container locally (if image built locally)
docker run -d `
  --name loan-management-system `
  -p 8080:8080 `
  -e SPRING_DATASOURCE_URL="jdbc:h2:mem:testdb" `
  docker.io/yourdockerusername/loan-management-system:1.0.0

# View logs
docker logs -f loan-management-system

# Test API
curl http://localhost:8080/api/v1/consumers

# Stop container
docker stop loan-management-system
docker rm loan-management-system
```

### 2. Docker Compose Deployment
```bash
# Update docker-compose.yml with your image
docker-compose up -d

# Verify services
docker-compose ps

# View logs
docker-compose logs -f loan-management-system

# Stop services
docker-compose down
```

### 3. Production Deployment
- Update image reference in Kubernetes manifests
- Configure resource limits (CPU, memory)
- Set environment variables for production database
- Enable health checks and monitoring
- Use private registry if required

---

## Reference

- **Docker Image Naming**: https://docs.docker.com/engine/reference/commandline/tag/
- **Google Jib Documentation**: https://cloud.google.com/build/docs/building-container-images-with-jib
- **Docker Hub**: https://hub.docker.com/
- **OpenJDK Base Images**: https://hub.docker.com/r/library/openjdk

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven JIB plugin configuration |
| `docker-compose.yml` | Container orchestration for local development |
| `Dockerfile` | Reference Dockerfile (JIB generates optimal variant) |
| `DOCKER.md` | Original Docker documentation |

---

**Last Updated**: 2026-02-26  
**Status**: ✅ Configuration Fixed & Ready for Build
