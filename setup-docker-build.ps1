#!/usr/bin/env powershell
# Docker Build Setup Script for Loan Management System
# This script helps configure environment variables and build Docker images

param(
    [string]$DockerHubUser,
    [string]$DockerHubToken,
    [string]$BuildType = "local"
)

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Docker Build Setup Script" -ForegroundColor Cyan
Write-Host "Loan Management System" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "Step 1: Checking Prerequisites..." -ForegroundColor Green
Write-Host ""

# Check Docker
Write-Host "  • Checking Docker installation..." -NoNewline
try {
    docker --version | Out-Null
    Write-Host " ✓" -ForegroundColor Green
}
catch {
    Write-Host " ✗ Docker not installed" -ForegroundColor Red
    Write-Host "    Install Docker Desktop from https://www.docker.com/products/docker-desktop"
    exit 1
}

# Check Maven
Write-Host "  • Checking Maven installation..." -NoNewline
try {
    mvn --version | Out-Null
    Write-Host " ✓" -ForegroundColor Green
}
catch {
    Write-Host " ✗ Maven not installed" -ForegroundColor Red
    Write-Host "    Install Maven from https://maven.apache.org/download.cgi"
    exit 1
}

# Check Java
Write-Host "  • Checking Java installation..." -NoNewline
try {
    $javaVersion = java -version 2>&1
    if ($javaVersion -match "17") {
        Write-Host " ✓" -ForegroundColor Green
    } else {
        Write-Host " ⚠ (Java 17 recommended)" -ForegroundColor Yellow
    }
}
catch {
    Write-Host " ✗ Java not installed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Configuring Docker Hub Credentials..." -ForegroundColor Green
Write-Host ""

# Get Docker Hub credentials
if (-not $DockerHubUser) {
    $DockerHubUser = Read-Host "Enter your Docker Hub username (must be lowercase)"
    $DockerHubUser = $DockerHubUser.ToLower()
}
else {
    $DockerHubUser = $DockerHubUser.ToLower()
}

if (-not $DockerHubToken) {
    $SecureToken = Read-Host "Enter your Docker Hub personal access token" -AsSecureString
    $DockerHubToken = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($SecureToken))
}

# Set environment variables
$env:DOCKER_HUB_USER = $DockerHubUser
$env:DOCKER_HUB_TOKEN = $DockerHubToken

Write-Host "  • Docker Hub User: $DockerHubUser" -ForegroundColor Cyan
Write-Host "  • Docker Hub Token: ***" -ForegroundColor Cyan

Write-Host ""
Write-Host "Step 3: Verifying Docker Hub Access..." -ForegroundColor Green
Write-Host ""

Write-Host "  • Testing Docker Hub login..." -NoNewline
$loginResult = docker login -u $env:DOCKER_HUB_USER -p $env:DOCKER_HUB_TOKEN 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host " ✓" -ForegroundColor Green
}
else {
    Write-Host " ✗" -ForegroundColor Red
    Write-Host "    Error: Invalid credentials"
    Write-Host "    Please check your Docker Hub username and token"
    exit 1
}

Write-Host ""
Write-Host "Step 4: Building Application..." -ForegroundColor Green
Write-Host ""

$projectRoot = Get-Location
Write-Host "  • Project Root: $projectRoot" -ForegroundColor Cyan

Write-Host "  • Running Maven clean compile..." -NoNewline
$compileResult = mvn clean compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host " ✓" -ForegroundColor Green
}
else {
    Write-Host " ✗" -ForegroundColor Red
    Write-Host "    Compilation failed. Check pom.xml and source code."
    exit 1
}

Write-Host ""
Write-Host "Step 5: Creating Docker Image..." -ForegroundColor Green
Write-Host ""

switch ($BuildType) {
    "local" {
        Write-Host "  • Building local Docker image (jib:dockerBuild)..." -NoNewline
        mvn clean package -DskipTests jib:dockerBuild -q 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host " ✓" -ForegroundColor Green
            Write-Host ""
            Write-Host "  • Docker image created successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "    Image: docker.io/$DockerHubUser/loan-management-system:1.0.0" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "    To run the container:" -ForegroundColor Green
            Write-Host "    docker run -p 8080:8080 docker.io/$DockerHubUser/loan-management-system:1.0.0" -ForegroundColor Cyan
        }
        else {
            Write-Host " ✗" -ForegroundColor Red
            Write-Host "    Build failed. Check Docker daemon and pom.xml configuration."
            exit 1
        }
    }
    "push" {
        Write-Host "  • Building and pushing to Docker Hub (jib:build)..." -NoNewline
        mvn clean package -DskipTests jib:build -q 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host " ✓" -ForegroundColor Green
            Write-Host ""
            Write-Host "  • Image pushed to Docker Hub successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "    Image: docker.io/$DockerHubUser/loan-management-system:1.0.0" -ForegroundColor Cyan
            Write-Host "    Repository: https://hub.docker.com/r/$DockerHubUser/loan-management-system" -ForegroundColor Cyan
        }
        else {
            Write-Host " ✗" -ForegroundColor Red
            Write-Host "    Push failed. Check credentials and Docker Hub repository permissions."
            exit 1
        }
    }
}

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "Setup Complete! ✓" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Green
Write-Host "  1. Review DOCKER_BUILD_GUIDE.md for detailed instructions"
Write-Host "  2. Run: docker-compose up -d (for full stack deployment)"
Write-Host "  3. Access: http://localhost:8080/swagger-ui.html"
Write-Host ""
