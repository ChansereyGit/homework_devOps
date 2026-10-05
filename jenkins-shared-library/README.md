# Jenkins Shared Library - DevOps CI/CD Pipeline

A comprehensive Jenkins Shared Library that provides reusable pipeline functions for building, testing, and deploying **ReactJS** and **Spring Boot** applications with Docker.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Available Functions](#available-functions)
- [Dockerfiles](#dockerfiles)
- [Usage Examples](#usage-examples)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

This shared library provides a complete CI/CD solution with:

- **Dockerfiles as Resources**: Pre-built, optimized Dockerfiles for ReactJS and Spring Boot
- **Build Functions**: Build Docker images using library Dockerfiles
- **Push Functions**: Push images to Docker Hub, AWS ECR, Google GCR
- **Deploy Functions**: Deploy containers to Jenkins server or remote hosts
- **SonarQube Integration**: Code quality scanning with quality gate checks
- **Telegram Notifications**: Real-time build notifications with status updates
- **Multi-environment Support**: Development, staging, production configurations

---

## ✨ Features

### 🐳 Docker Management
- Multi-stage Dockerfiles for optimal image sizes
- Build images from library resources
- Push to multiple registries (Docker Hub, ECR, GCR)
- Multi-tag support
- Image verification

### 🔍 Code Quality
- SonarQube scanning for ReactJS and Spring Boot
- Quality gate enforcement
- Automatic project detection (Gradle/Maven)
- Detailed quality reports

### 🚀 Deployment
- Container deployment with health checks
- Docker Compose support
- Environment variable management
- Rollback capabilities
- Container monitoring and logs

### 📱 Notifications
- Telegram integration
- Build status notifications
- Stage-level updates
- Failure alerts with logs

---

## 📁 Project Structure

```
jenkins-shared-library/
├── vars/                           # Global pipeline functions
│   ├── buildDockerImage.groovy    # Build Docker images
│   ├── pushDockerImage.groovy     # Push to registries
│   ├── deployContainer.groovy     # Deploy containers
│   ├── scanSonarqube.groovy       # SonarQube scanning
│   └── sendTelegram.groovy        # Telegram notifications
├── resources/                      # Dockerfile templates
│   ├── reactjs.Dockerfile         # ReactJS multi-stage Dockerfile
│   ├── spring.Dockerfile          # Spring Boot multi-stage Dockerfile
│   └── nginx.conf                 # Nginx config for ReactJS
├── src/                            # Groovy classes (if needed)
│   └── com/
│       └── devops/
├── examples/                       # Sample pipelines
│   ├── reactjs-pipeline.groovy    # Complete ReactJS pipeline
│   └── spring-pipeline.groovy     # Complete Spring Boot pipeline
├── README.md                       # This file
└── SETUP.md                        # Setup instructions
```

---

## 🔧 Prerequisites

### Jenkins Setup
- Jenkins 2.400+
- Required Plugins:
  - Pipeline
  - Docker Pipeline
  - SonarQube Scanner
  - Telegram Notifier (optional)
  - Git

### Tools
- Docker installed on Jenkins server
- SonarQube server (optional)
- Telegram Bot (optional for notifications)

### Credentials
Configure in Jenkins Credentials:
- `dockerhub-credentials` - Docker Hub username/password
- `SONARQUBE-TOKEN` - SonarQube authentication token
- `telegram-bot-token` - Telegram bot token
- `telegram-chat-id` - Telegram chat ID

---

## 🚀 Quick Start

### 1. Add Library to Jenkins

**Option A: Global Trusted Library**
1. Navigate to: **Manage Jenkins** → **Configure System** → **Global Pipeline Libraries**
2. Add library:
   - **Name**: `devops-shared-library`
   - **Default version**: `main`
   - **Retrieval method**: Modern SCM
   - **Source Code Management**: Git
   - **Project Repository**: `https://github.com/your-org/jenkins-shared-library.git`
   - ✅ Check **Allow default version to be overridden**
   - ✅ Check **Include @Library changes in job recent changes**

**Option B: Folder-level Library**
1. Create a folder in Jenkins
2. Configure folder properties
3. Add library configuration at folder level

### 2. Use in Your Pipeline

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                script {
                    buildDockerImage.reactjs("my-app", "v1.0.0")
                }
            }
        }
    }
}
```

---

## 📚 Available Functions

### 🔨 buildDockerImage

Build Docker images using Dockerfiles from library resources.

```groovy
// Build ReactJS application
buildDockerImage.reactjs("my-react-app", "v1.0.0", "myregistry")

// Build Spring Boot application
buildDockerImage.spring("my-spring-app", "v2.0.0", "myregistry")

// Generic build with custom Dockerfile
buildDockerImage("my-app", "v1.0.0", "reactjs", ".", [NODE_ENV: "production"])

// Build with multiple tags
buildDockerImage.multiTag("my-app", ["v1.0.0", "latest", "prod"], "reactjs")

// Get image size
def size = buildDockerImage.getImageSize("my-app:v1.0.0")
```

**Parameters:**
- `imageName` - Docker image name
- `tag` - Image tag (default: BUILD_NUMBER)
- `dockerfileType` - Type: "reactjs" or "spring"
- `contextPath` - Build context (default: ".")
- `buildArgs` - Map of build arguments

---

### 🚀 pushDockerImage

Push Docker images to registries.

```groovy
// Push to Docker Hub
pushDockerImage("myuser/my-app:v1.0.0", "dockerhub-credentials")

// Push multiple tags
pushDockerImage.multiTag("myuser/my-app", ["v1.0.0", "latest"], "dockerhub-credentials")

// Push to AWS ECR
pushDockerImage.toECR("my-app:v1.0.0", "aws-credentials", "us-east-1", "123456789")

// Push to Google GCR
pushDockerImage.toGCR("my-app:v1.0.0", "gcp-credentials", "my-project")

// Verify image exists
def exists = pushDockerImage.verifyImage("my-app:v1.0.0", "dockerhub-credentials")
```

**Parameters:**
- `imageName` - Full image name with tag
- `credentialsId` - Jenkins credentials ID
- `registry` - Registry URL (optional for Docker Hub)

---

### 🚢 deployContainer

Deploy containers to Jenkins server or remote hosts.

```groovy
// Deploy ReactJS app
deployContainer.reactjs("my-app:v1.0.0", "my-app-prod", "8080")

// Deploy Spring Boot app with environment variables
deployContainer.spring(
    "my-spring-app:v1.0.0", 
    "spring-prod", 
    "8080",
    [
        SPRING_PROFILES_ACTIVE: "production",
        DATABASE_URL: "postgres://db:5432"
    ]
)

// Deploy with volumes and network
deployContainer(
    "my-app:v1.0.0",
    "my-app",
    "8080:80",
    [API_KEY: "secret"],
    ["/host/data:/data"],
    "my-network"
)

// Health check
def healthy = deployContainer.healthCheck(
    "my-app", 
    "http://localhost:8080/health",
    10,  // max retries
    5    // retry delay in seconds
)

// Get container logs
deployContainer.getLogs("my-app", 50)

// Get container status
def status = deployContainer.getStatus("my-app")
```

**Parameters:**
- `imageName` - Full image name with tag
- `containerName` - Container name
- `port` - Port mapping (e.g., "8080:80")
- `envVars` - Environment variables map
- `volumes` - Volume mappings list
- `network` - Docker network name

---

### 🔍 scanSonarqube

Run SonarQube code quality analysis.

```groovy
// Scan ReactJS project
scanSonarqube.reactjs("My React App", "1.0.0", "my-react-app")

// Scan Spring Boot project (auto-detects Gradle/Maven)
scanSonarqube.spring("My Spring App", "2.0.0", "my-spring-app")

// Generic scan with custom parameters
scanSonarqube(
    "My Project",
    "1.0.0",
    "my-project",
    "src",
    "**/test/**,**/node_modules/**"
)

// Wait for Quality Gate
def qg = scanSonarqube.waitForQualityGate(5) // 5 minutes timeout
if (qg.status != 'OK') {
    error("Quality Gate failed")
}

// Get dashboard URL
def url = scanSonarqube.getDashboardUrl("my-project")
```

**Parameters:**
- `projectName` - Project display name
- `projectVersion` - Project version
- `projectKey` - Unique project key
- `sources` - Source directories (default: "src")
- `exclusions` - Files to exclude

---

### 📱 sendTelegram

Send Telegram notifications.

```groovy
// Simple message
sendTelegram(
    "Build completed successfully!",
    "${env.TELEGRAM_TOKEN}",
    "${env.TELEGRAM_CHAT_ID}"
)

// Markdown message
def message = """
*Build Success* ✅

Project: `${env.JOB_NAME}`
Build: `#${env.BUILD_NUMBER}`

[View Build](${env.BUILD_URL})
"""
sendTelegram(message, "${TOKEN}", "${CHAT_ID}")

// Build notification
sendTelegram.buildNotification("SUCCESS", "${TOKEN}", "${CHAT_ID}")

// Stage notification
sendTelegram.stageNotification("Deploy", "SUCCESS", "${TOKEN}", "${CHAT_ID}")
```

**Parameters:**
- `message` - Message text (supports Markdown)
- `token` - Telegram bot token
- `chatId` - Telegram chat ID
- `parseMode` - Parse mode (default: "Markdown")

---

## 🐳 Dockerfiles

### ReactJS Dockerfile

**Features:**
- Multi-stage build (Node.js + Nginx)
- Production optimized
- Health checks included
- Gzip compression
- Security headers
- SPA routing support

**Build Stages:**
1. **Builder**: Installs dependencies and builds React app
2. **Production**: Nginx serves static files

**Size:** ~25MB (compared to ~1GB with Node.js)

---

### Spring Boot Dockerfile

**Features:**
- Multi-stage build (Gradle + JRE)
- Non-root user execution
- JVM optimization
- Health checks via Actuator
- Minimal Alpine base image

**Build Stages:**
1. **Builder**: Compiles Java code with Gradle
2. **Production**: Runs JAR with optimized JVM settings

**Size:** ~200MB (compared to ~800MB with full JDK)

---

## 💡 Usage Examples

### Complete ReactJS Pipeline

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    environment {
        IMAGE_NAME = "myuser/react-app"
        IMAGE_TAG = "v1.0.${BUILD_NUMBER}"
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/myuser/react-app.git'
            }
        }
        
        stage('SonarQube Scan') {
            steps {
                script {
                    scanSonarqube.reactjs("React App", "1.0.0", "react-app")
                    def qg = scanSonarqube.waitForQualityGate()
                    if (qg.status != 'OK') error("Quality Gate failed")
                }
            }
        }
        
        stage('Build Image') {
            steps {
                script {
                    buildDockerImage.reactjs("${IMAGE_NAME}", "${IMAGE_TAG}")
                }
            }
        }
        
        stage('Push Image') {
            steps {
                script {
                    pushDockerImage("${IMAGE_NAME}:${IMAGE_TAG}", "dockerhub-creds")
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    deployContainer.reactjs(
                        "${IMAGE_NAME}:${IMAGE_TAG}",
                        "react-app-prod",
                        "3000"
                    )
                    
                    deployContainer.healthCheck(
                        "react-app-prod",
                        "http://localhost:3000"
                    )
                }
            }
        }
    }
    
    post {
        success {
            script {
                sendTelegram.buildNotification(
                    "SUCCESS",
                    "${TELEGRAM_TOKEN}",
                    "${TELEGRAM_CHAT_ID}"
                )
            }
        }
        failure {
            script {
                sendTelegram.buildNotification(
                    "FAILURE",
                    "${TELEGRAM_TOKEN}",
                    "${TELEGRAM_CHAT_ID}"
                )
            }
        }
    }
}
```

### Complete Spring Boot Pipeline

See `examples/spring-pipeline.groovy` for a full example with:
- Unit tests
- SonarQube scanning
- Gradle build
- Docker build and push
- Deployment with environment variables
- Smoke tests
- Comprehensive notifications

---

## ⚙️ Configuration

### Jenkins Credentials Setup

1. **Docker Hub**
   - Type: Username with password
   - ID: `dockerhub-credentials`
   - Username: Your Docker Hub username
   - Password: Your Docker Hub password/token

2. **SonarQube**
   - Type: Secret text
   - ID: `SONARQUBE-TOKEN`
   - Secret: Your SonarQube authentication token

3. **Telegram**
   - Type: Secret text
   - ID: `telegram-bot-token`
   - Secret: Your Telegram bot token
   - 
   - Type: Secret text
   - ID: `telegram-chat-id`
   - Secret: Your Telegram chat ID

### SonarQube Tool Configuration

1. Navigate to: **Manage Jenkins** → **Global Tool Configuration**
2. Find **SonarQube Scanner** section
3. Add SonarQube Scanner:
   - **Name**: `sonar-scanner`
   - **Install automatically**: ✅ Checked
   - **Version**: Latest

### SonarQube Server Configuration

1. Navigate to: **Manage Jenkins** → **Configure System**
2. Find **SonarQube servers** section
3. Add SonarQube server:
   - **Name**: `sonar-scanner`
   - **Server URL**: `http://your-sonarqube-server:9000`
   - **Server authentication token**: Select `SONARQUBE-TOKEN` credential

---

## 🔧 Troubleshooting

### Issue: "libraryResource not found"

**Cause:** Dockerfile not in resources folder or wrong path

**Solution:**
```bash
# Ensure files are in resources/ folder
jenkins-shared-library/
└── resources/
    ├── reactjs.Dockerfile
    ├── spring.Dockerfile
    └── nginx.conf
```

### Issue: Docker permission denied

**Solution:**
```bash
# Add Jenkins user to docker group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Issue: SonarQube scanner not found

**Solution:**
1. Ensure sonar-scanner tool is configured in Jenkins
2. Tool name must match: `sonar-scanner`
3. Check SonarQube server connection

### Issue: Telegram notifications not working

**Solution:**
1. Verify bot token is correct
2. Check chat ID is correct
3. Ensure bot is added to chat/channel
4. Test with curl:
```bash
curl -X POST "https://api.telegram.org/bot<TOKEN>/sendMessage" \
  -d chat_id="<CHAT_ID>" \
  -d text="Test message"
```

### Issue: Container health check fails

**Solution:**
1. Check container logs: `deployContainer.getLogs("container-name")`
2. Verify health endpoint is correct
3. Increase retry count and delay
4. Check container is actually running

---

## 📝 Best Practices

1. **Version Your Library**: Always specify library version in pipeline
   ```groovy
   @Library('devops-shared-library@v1.0.0') _
   ```

2. **Use Credentials**: Never hardcode sensitive data
   ```groovy
   environment {
       TOKEN = credentials('my-token')
   }
   ```

3. **Tag Images Properly**: Use semantic versioning
   ```groovy
   IMAGE_TAG = "v1.0.${BUILD_NUMBER}"
   ```

4. **Health Checks**: Always verify deployment
   ```groovy
   deployContainer.healthCheck("app", "http://localhost:8080/health")
   ```

5. **Clean Up**: Remove old images
   ```groovy
   post {
       always {
           sh 'docker image prune -f'
       }
   }
   ```

---

## 📚 Additional Resources

- [Jenkins Shared Library Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)
- [Docker Documentation](https://docs.docker.com/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Telegram Bot API](https://core.telegram.org/bots/api)

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Authors

- DevOps Team

---

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Contact DevOps team
- Check troubleshooting section above

---

**Happy Building! 🚀**
