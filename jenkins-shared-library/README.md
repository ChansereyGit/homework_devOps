# Jenkins Shared Library

CI/CD pipeline functions for Docker, SonarQube, and Telegram.

## Structure
```
jenkins-shared-library/
├── vars/          
│   ├── buildDockerImage.groovy
│   ├── pushDockerImage.groovy
│   ├── deployContainer.groovy
│   ├── scanSonarqube.groovy
│   └── sendTelegram.groovy
├── resources/     
│   ├── reactjs.Dockerfile
│   ├── spring.Dockerfile
│   └── nginx.conf
└── examples/
    ├── reactjs-pipeline.groovy
    └── spring-pipeline.groovy
```

## Functions

**buildDockerImage(imageName, tag, dockerfileType)**
```groovy
buildDockerImage("chanserey/my-app", "v1.0.0", "reactjs")
```

**pushDockerImage(imageName, tag, credentialsId)**
```groovy
pushDockerImage("chanserey/my-app", "v1.0.0", "dockerhub-credentials")
```

**deployContainer(imageName, tag, containerName, port)**
```groovy
deployContainer("chanserey/my-app", "v1.0.0", "my-app", "3000")
```

**scanSonarqube(projectName, projectVersion, projectKey)**
```groovy
scanSonarqube("My Project", "1.0.0", "my-project")
```

**sendTelegram(message, token, chatId)**
```groovy
sendTelegram("Build completed", "${TOKEN}", "${CHAT_ID}")
```

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
        stage("Checkout"){ steps{ git 'https://github.com/your-repo.git' } }
        stage("Build"){ steps{ script{ buildDockerImage("${FULL_IMG}", "${TAG}", "reactjs") } } }
        stage("Push"){ steps{ script{ pushDockerImage("${DH_USER}/${IMG_NAME}", "${TAG}", "dockerhub-credentials") } } }
        stage("Deploy"){ steps{ script{ deployContainer("${DH_USER}/${IMG_NAME}", "${TAG}", "my-app", "3000") } } }
    }
    post {
        success { script { sendTelegram("Build Success","${TOKEN}","${CHAT_ID}") } }
    }
}
```
