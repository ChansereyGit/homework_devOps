# DevOps Homework Repository

This repository contains completed homework assignments for DevOps course.

## 📁 Structure

```
homework_devOps/
├── ansible-homework/          # Ansible GCP instance automation
├── jenkins-shared-library/    # Jenkins shared library with Dockerfiles
└── demo-apps/                 # Demo applications for testing pipelines
    ├── react-demo-app/        # ReactJS demo application
    └── spring-demo-app/       # Spring Boot demo application
```

## 📚 Homework 1: Ansible Automation

**Location:** `ansible-homework/`

Automated GCP instance management with Ansible:
- Creates 2 masters (e2-standard-2, 50GB disk)
- Creates 2 workers (e2-medium, 40GB disk)
- Auto-updates inventory with new IPs
- Tests SSH connectivity
- Includes deletion playbook

**Usage:**
```bash
cd ansible-homework
ansible-playbook playbook/create-instances.yaml
ansible-playbook playbook/delete-instances.yaml
```

## 📚 Homework 2: Jenkins Shared Library

**Location:** `jenkins-shared-library/`

Complete CI/CD shared library featuring:

### 🐳 Dockerfiles as Resources
- `reactjs.Dockerfile` - Multi-stage build for ReactJS
- `spring.Dockerfile` - Multi-stage build for Spring Boot
- `nginx.conf` - Optimized nginx configuration

### 🔧 Pipeline Functions

**buildDockerImage**
- Build Docker images using library Dockerfiles
- Support for ReactJS and Spring Boot
- Multi-tag capability

**pushDockerImage**
- Push to Docker Hub, AWS ECR, Google GCR
- Multi-tag push support
- Image verification

**deployContainer**
- Deploy containers to Jenkins server
- Health check monitoring
- Container logs and status

**scanSonarqube**
- Code quality scanning
- Quality gate enforcement
- ReactJS and Spring Boot support

**sendTelegram**
- Build notifications
- Stage notifications
- Custom messages with Markdown

### 📦 Demo Applications

**React Demo App** (`demo-apps/react-demo-app/`)
- Simple React application
- Modern gradient UI
- Build information display
- Jenkinsfile included

**Spring Boot Demo App** (`demo-apps/spring-demo-app/`)
- REST API with Spring Boot
- Health check endpoints
- Actuator integration
- Unit tests included
- Jenkinsfile included

### 🚀 Quick Start

1. **Configure Jenkins:**
   ```bash
   # See jenkins-shared-library/QUICKSTART.md
   ```

2. **Add Shared Library:**
   - Manage Jenkins → Configure System
   - Add Global Pipeline Library
   - Name: `devops-shared-library`
   - Repository: This repo

3. **Configure Credentials:**
   - `dockerhub-credentials` (chanserey/password)
   - `telegram-bot-token` (8905728861:AAF7U1YF5er_uPXUiYBxFQ9qAa3iv0jeVUc)
   - `telegram-chat-id` (Your chat ID)

4. **Test Pipeline:**
   ```groovy
   @Library('devops-shared-library@main') _

   pipeline {
       agent any
       stages {
           stage('Test') {
               steps {
                   script {
                       sendTelegram("Works!", 
                           credentials('telegram-bot-token'), 
                           credentials('telegram-chat-id'))
                   }
               }
           }
       }
   }
   ```

### 📖 Documentation

- **[SETUP.md](jenkins-shared-library/SETUP.md)** - Complete setup guide
- **[QUICKSTART.md](jenkins-shared-library/QUICKSTART.md)** - Fast setup with pre-configured values
- **[README.md](jenkins-shared-library/README.md)** - Full documentation
- **[HOMEWORK_SUMMARY.md](jenkins-shared-library/HOMEWORK_SUMMARY.md)** - Homework requirements and completion

### 🎯 Pipeline Examples

Complete working examples in `jenkins-shared-library/examples/`:
- `reactjs-pipeline.groovy` - Full ReactJS CI/CD pipeline
- `spring-pipeline.groovy` - Full Spring Boot CI/CD pipeline

Both include:
- Checkout from Git
- SonarQube scanning
- Quality gate enforcement
- Docker build using library Dockerfiles
- Push to Docker Hub
- Container deployment
- Health checks
- Telegram notifications

## 🛠️ Technologies Used

### Ansible Homework
- Ansible
- Google Cloud Platform (GCP)
- Compute Engine

### Jenkins Homework
- Jenkins
- Docker
- SonarQube
- Telegram Bot API
- ReactJS
- Spring Boot
- Gradle
- Nginx

## 📝 Author

**Chanserey**
- Docker Hub: https://hub.docker.com/u/chanserey
- Telegram Bot: @reiJenkinsBot

## 🎓 Course

DevOps Training Course
- Ansible automation
- Jenkins CI/CD
- Docker containerization
- Infrastructure as Code

---

**All homework assignments are complete and ready for submission! ✅**
