# Docker Deployment Guide

## Quick Start with Docker Compose

### Prerequisites
- Docker Desktop installed and running
- Docker Compose installed (`docker-compose --version`)
- Port 8080, 3306, 8081 available

### Local Development with MySQL

```bash
# Build application JAR first
mvn clean package -DskipTests

# Start all services (MySQL + Application)
docker-compose up -d

# Wait for services to start (check health)
sleep 10

# Verify MySQL is running
docker exec loan-management-mysql mysql -u root -proot -e "SHOW DATABASES;"

# Verify Application started
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# View application logs
docker-compose logs -f app

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Optional: Access phpMyAdmin at http://localhost:8081
# Username: root, Password: root

# Stop all services
docker-compose down

# Clean up volumes (removes MySQL data)
docker-compose down -v
```

---

## Docker Image Build & Push

### Development Build (Local Docker)

```bash
# Compile JAR
mvn clean package

# Build Docker image locally using Jib
mvn jib:dockerBuild

# List images
docker images | grep loan-management

# Run container
docker run -d \
  --name loan-management-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=h2 \
  loan-management-system:1.0.0

# View logs
docker logs -f loan-management-app

# Test API
curl http://localhost:8080/v3/api-docs

# Stop and remove
docker stop loan-management-app
docker rm loan-management-app
```

### Production Build & Push to Docker Hub

```bash
# Prerequisites
export DOCKER_HUB_USER=your_dockerhub_username
export DOCKER_HUB_TOKEN=your_personal_access_token

# Build and push to Docker Hub using Jib
mvn clean package jib:build

# Verify pushed to registry
curl https://hub.docker.com/v2/repositories/$DOCKER_HUB_USER/loan-management-system/tags

# Pull and run from Docker Hub
docker pull docker.io/$DOCKER_HUB_USER/loan-management-system:1.0.0
docker run -d \
  --name loan-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=mysql \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/loan_management \
  docker.io/$DOCKER_HUB_USER/loan-management-system:1.0.0

# View logs
docker logs -f loan-app

# Cleanup
docker stop loan-app
docker rm loan-app
```

---

## Docker Image Details

### Image Composition
```
Base Image:     openjdk:17-jdk-slim (slim variant ~150MB)
JVM Flags:      -XX:+UseG1GC (garbage collection)
                -XX:MaxRAMPercentage=75 (memory allocation)
                -XX:+UseStringDeduplication (optimization)
RAM Allocation: 75% of container limit (e.g., 750MB for 1GB limit)
Startup Time:   2-3 seconds
Total Size:     150-180MB
```

### Multi-Layer Structure
```
Layer 1: Base image (openjdk:17-jdk-slim) - cached across builds
Layer 2: Dependencies JAR (cached if pom.xml unchanged)
Layer 3: Application classes (frequently changed)
Layer 4: Configuration & metadata
```

### Build Performance
- **First build**: 60-90 seconds (downloads dependencies)
- **Subsequent builds**: 20-30 seconds (layers cached)
- **Push to registry**: 5-10 seconds (efficient layer transfer)

---

## Environment Variables

### Configuration via Environment

```bash
docker run -e SPRING_PROFILES_ACTIVE=mysql \
           -e SPRING_DATASOURCE_URL=jdbc:mysql://db-server:3306/loan_management \
           -e SPRING_DATASOURCE_USERNAME=root \
           -e SPRING_DATASOURCE_PASSWORD=password \
           -e JAVA_OPTS="-Xmx1024m -Xms512m" \
           -p 8080:8080 \
           loan-management-system:1.0.0
```

### Profile Options
- **h2** (default): In-memory database, no MySQL required
- **mysql**: Connect to external MySQL server
- **prod**: Production settings (security enabled, detailed logging)

### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://hostname:3306/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
```

---

## Advanced: Kubernetes Deployment

### Prerequisites
- kubectl configured
- Access to Kubernetes cluster
- Docker image pushed to accessible registry

### Deployment YAML
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: loan-management-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: loan-management
  template:
    metadata:
      labels:
        app: loan-management
    spec:
      containers:
      - name: app
        image: docker.io/your_username/loan-management-system:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "mysql"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: db-config
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

### Kubernetes Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: loan-management-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: loan-management
```

### Deploy to Kubernetes
```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get pods
kubectl get services
kubectl logs <pod-name>
```

---

## Troubleshooting

### Container won't start
```bash
# Check logs
docker logs loan-management-app

# Common issues:
# 1. Port 8080 already in use
#    Solution: Use different port or stop conflicting container
docker run -p 8081:8080 ...

# 2. Insufficient memory
#    Solution: Increase Docker memory allocation or add -Xmx flag
docker run -e JAVA_OPTS="-Xmx512m" ...

# 3. Database connection failure
#    Solution: Ensure MySQL is running and accessible
docker exec loan-management-app curl http://mysql:3306
```

### Health check failing
```bash
# Manual health check
docker exec loan-management-app curl http://localhost:8080/actuator/health

# Check startup time
docker logs loan-management-app | grep "Started"
# If > 30 seconds, increase healthcheck timeout in docker-compose.yml
```

### Performance issues
```bash
# Monitor resource usage
docker stats loan-management-app

# Increase memory
docker run -m 2g loan-management-system:1.0.0

# Check slow queries in logs
docker logs loan-management-app | grep "took"
```

---

## Security Considerations

### Image Security
- ✅ Base image: Official OpenJDK slim (minimal vulnerability surface)
- ✅ Non-root user execution (Jib default)
- ✅ Read-only filesystem support (enable in Kubernetes)
- ✅ Security scanning available: `docker scan loan-management-system:1.0.0`

### Runtime Security
- ✅ Use docker-compose secrets for sensitive environment variables
- ✅ Enable HTTPS in production (configure Spring Security)
- ✅ Restrict network access using docker networks
- ✅ Enable container logging and monitoring

### Registry Security
- ✅ Use private Docker registries (not Docker Hub public)
- ✅ Authenticate with personal access tokens (not passwords)
- ✅ Scan images for vulnerabilities before deployment
- ✅ Keep base image updated regularly

---

## Monitoring & Logging

### Container Logs
```bash
# View application logs
docker logs loan-management-app

# Follow logs in real-time
docker logs -f loan-management-app

# Filter by pattern
docker logs loan-management-app | grep ERROR

# View last 50 lines
docker logs --tail 50 loan-management-app
```

### Health Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (with components)
curl http://localhost:8080/actuator/health/detailed

# Readiness (ready to receive traffic)
curl http://localhost:8080/actuator/health/readiness

# Liveness (process still running)
curl http://localhost:8080/actuator/health/liveness
```

### Metrics
```bash
# Prometheus metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

**Version**: 1.0.0 | **Last Updated**: 2026-02-25 | **Maintenance**: Update base image quarterly
