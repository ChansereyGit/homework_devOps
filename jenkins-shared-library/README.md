# Jenkins Shared Library

Shared library for Jenkins CI/CD pipelines with Docker, SonarQube, and Telegram integration.

## Structure

```
jenkins-shared-library/
├── vars/                    # Shared library functions
│   ├── buildDockerImage.groovy
│   ├── pushDockerImage.groovy
│   ├── deployContainer.groovy
│   ├── scanSonarqube.groovy
│   └── sendTelegram.groovy
├── resources/               # Dockerfiles
│   ├── reactjs.Dockerfile
│   ├── spring.Dockerfile
│   └── nginx.conf
└── examples/               # Example pipelines
    ├── reactjs-pipeline.groovy
    └── spring-pipeline.groovy
```

## Functions

### buildDockerImage
Build Docker image using Dockerfile from resources.

```groovy
buildDockerImage("chanserey/my-app", "v1.0.0", "reactjs")
```

### pushDockerImage
Push Docker image to Docker Hub.

```groovy
pushDockerImage("chanserey/my-app", "v1.0.0", "dockerhub-credentials")
```

### deployContainer
Deploy container on Jenkins machine.

```groovy
deployContainer("chanserey/my-app", "v1.0.0", "my-app", "3000")
```

### scanSonarqube
Scan code with SonarQube.

```groovy
scanSonarqube("My Project", "1.0.0", "my-project")
```

### sendTelegram
Send message to Telegram.

```groovy
sendTelegram("Build completed", "${TOKEN}", "${CHAT_ID}")
```

## Setup

### 1. Configure Shared Library in Jenkins

Manage Jenkins → Configure System → Global Pipeline Libraries

- Name: `devops-shared-library`
- Default version: `main`
- Repository: `https://github.com/ChansereyGit/homework_devOps.git`

### 2. Configure Credentials

- `dockerhub-credentials`: Docker Hub username/password
- `telegram-bot-token`: Telegram bot token
- `telegram-chat-id`: Telegram chat ID
- `SONARQUBE-TOKEN`: SonarQube token

### 3. Configure Tools

Manage Jenkins → Global Tool Configuration

- Add SonarQube Scanner with name: `sonar-scanner`

### 4. Configure SonarQube Server

Manage Jenkins → Configure System → SonarQube servers

- Name: `sonar-scanner`
- Server URL: Your SonarQube URL
- Token: Use `SONARQUBE-TOKEN` credential

## Usage

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any

    environment{
        TAG="v1.0.${env.BUILD_NUMBER}"
        IMG_NAME="my-app"
        DH_USER="chanserey"
        FULL_IMG="${DH_USER}/${IMG_NAME}:${TAG}"
        CHAT_ID=credentials('telegram-chat-id')
        TOKEN=credentials('telegram-bot-token')
    }

    stages {
        stage("Checkout"){
            steps{
                git 'https://github.com/your-repo.git'
            }
        }

        stage("Build"){
            steps{
                script{
                    buildDockerImage("${FULL_IMG}", "${TAG}", "reactjs")
                }
            }
        }

        stage("Push"){
            steps{
                script{
                    pushDockerImage("${DH_USER}/${IMG_NAME}", "${TAG}", "dockerhub-credentials")
                }
            }
        }

        stage("Deploy"){
            steps{
                script{
                    deployContainer("${DH_USER}/${IMG_NAME}", "${TAG}", "my-app", "3000")
                }
            }
        }
    }

    post {
        success {
            script {
                sendTelegram("Build Success","${TOKEN}","${CHAT_ID}")
            }
        }
    }
}
```

## Dockerfiles

### reactjs.Dockerfile
Multi-stage build for ReactJS with Nginx.

### spring.Dockerfile
Multi-stage build for Spring Boot with Gradle.

## Demo Apps

See `demo-apps/` folder for:
- `react-demo-app` - ReactJS demo with Jenkinsfile
- `spring-demo-app` - Spring Boot demo with Jenkinsfile
