@Library('devops-shared-library@main') _

/**
 * Complete CI/CD Pipeline for ReactJS Application
 * 
 * This pipeline demonstrates:
 * - Using shared library functions
 * - Building with Dockerfile from library resources
 * - SonarQube code quality scanning
 * - Docker image build and push
 * - Container deployment on Jenkins server
 * - Telegram notifications
 */

pipeline {
    agent any
    
    environment {
        // Docker Configuration
        DOCKER_REGISTRY = "docker.io"
        DOCKER_USER = "chanserey"
        IMAGE_NAME = "${DOCKER_USER}/my-react-app"
        IMAGE_TAG = "v1.0.${env.BUILD_NUMBER}"
        FULL_IMAGE = "${IMAGE_NAME}:${IMAGE_TAG}"
        
        // Deployment Configuration
        CONTAINER_NAME = "react-app-prod"
        CONTAINER_PORT = "3000"
        
        // SonarQube Configuration
        SONAR_PROJECT_NAME = "My React App"
        SONAR_PROJECT_KEY = "my-react-app"
        SONAR_PROJECT_VERSION = "1.0.${env.BUILD_NUMBER}"
        
        // Telegram Configuration
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    stages {
        stage('📦 Checkout') {
            steps {
                script {
                    echo "Checking out code from repository..."
                    git branch: 'main', 
                        url: 'https://github.com/your-username/react-app.git'
                    
                    sendTelegram(
                        "🚀 *Build Started*\n\nProject: `${env.JOB_NAME}`\nBuild: `#${env.BUILD_NUMBER}`",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
        
        stage('🔍 SonarQube Analysis') {
            steps {
                script {
                    echo "Running SonarQube code quality scan..."
                    
                    try {
                        // Scan ReactJS project with SonarQube
                        scanSonarqube.reactjs(
                            "${SONAR_PROJECT_NAME}",
                            "${SONAR_PROJECT_VERSION}",
                            "${SONAR_PROJECT_KEY}"
                        )
                        
                        sendTelegram.stageNotification(
                            "SonarQube Analysis",
                            "SUCCESS",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        
                    } catch (Exception e) {
                        sendTelegram.stageNotification(
                            "SonarQube Analysis",
                            "FAILED",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
        
        stage('🏥 Quality Gate') {
            steps {
                script {
                    echo "Waiting for SonarQube Quality Gate result..."
                    
                    def qg = scanSonarqube.waitForQualityGate(5)
                    env.QG_STATUS = qg.status
                    
                    if (qg.status != 'OK') {
                        def message = """
❌ *QUALITY GATE FAILED*

📦 Project: `${env.JOB_NAME}`
🔢 Build: `#${env.BUILD_NUMBER}`
🔍 Status: `${qg.status}`

🔗 [View SonarQube Report](${scanSonarqube.getDashboardUrl(SONAR_PROJECT_KEY)})

⚠️ Please fix the code quality issues before proceeding.
"""
                        sendTelegram(message, "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
                        error("Quality Gate failed: ${qg.status}")
                    }
                    
                    sendTelegram(
                        "✅ Quality Gate Passed - Code quality is good!",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
        
        stage('🔨 Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image using shared library Dockerfile..."
                    
                    try {
                        // Build ReactJS image using Dockerfile from library resources
                        buildDockerImage.reactjs(
                            "${IMAGE_NAME}",
                            "${IMAGE_TAG}",
                            "${DOCKER_USER}"
                        )
                        
                        // Also tag as latest
                        sh "docker tag ${FULL_IMAGE} ${IMAGE_NAME}:latest"
                        
                        // Show image size
                        def imageSize = buildDockerImage.getImageSize("${FULL_IMAGE}")
                        echo "Image size: ${imageSize}"
                        
                        sendTelegram(
                            "🔨 Docker image built successfully\n\nImage: `${FULL_IMAGE}`\nSize: `${imageSize}`",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        
                    } catch (Exception e) {
                        sendTelegram.stageNotification(
                            "Build Docker Image",
                            "FAILED",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
        
        stage('🚀 Push to Registry') {
            steps {
                script {
                    echo "Pushing Docker image to registry..."
                    
                    try {
                        // Push both versioned and latest tags
                        pushDockerImage.multiTag(
                            "${IMAGE_NAME}",
                            ["${IMAGE_TAG}", "latest"],
                            "dockerhub-credentials"
                        )
                        
                        // Verify image in registry
                        def verified = pushDockerImage.verifyImage(
                            "${FULL_IMAGE}",
                            "dockerhub-credentials"
                        )
                        
                        if (verified) {
                            sendTelegram(
                                "🚀 Image pushed to registry\n\nImage: `${FULL_IMAGE}`\nRegistry: `${DOCKER_REGISTRY}`",
                                "${TELEGRAM_TOKEN}",
                                "${TELEGRAM_CHAT_ID}"
                            )
                        }
                        
                    } catch (Exception e) {
                        sendTelegram.stageNotification(
                            "Push to Registry",
                            "FAILED",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
        
        stage('🚢 Deploy Container') {
            steps {
                script {
                    echo "Deploying container to Jenkins server..."
                    
                    try {
                        // Deploy ReactJS container
                        deployContainer.reactjs(
                            "${FULL_IMAGE}",
                            "${CONTAINER_NAME}",
                            "${CONTAINER_PORT}"
                        )
                        
                        // Wait a bit for container to start
                        sleep 10
                        
                        // Health check
                        def isHealthy = deployContainer.healthCheck(
                            "${CONTAINER_NAME}",
                            "http://localhost:${CONTAINER_PORT}",
                            10,
                            5
                        )
                        
                        if (isHealthy) {
                            sendTelegram(
                                "🚢 Application deployed successfully!\n\nContainer: `${CONTAINER_NAME}`\nPort: `${CONTAINER_PORT}`\nImage: `${FULL_IMAGE}`",
                                "${TELEGRAM_TOKEN}",
                                "${TELEGRAM_CHAT_ID}"
                            )
                        } else {
                            error("Container health check failed")
                        }
                        
                    } catch (Exception e) {
                        deployContainer.getLogs("${CONTAINER_NAME}", 50)
                        
                        sendTelegram.stageNotification(
                            "Deploy Container",
                            "FAILED",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
    }
    
    post {
        success {
            script {
                def message = """
✅ *BUILD SUCCESS*

📦 *Project:* `${env.JOB_NAME}`
🔢 *Build:* `#${env.BUILD_NUMBER}`
🏷️ *Image:* `${FULL_IMAGE}`
🌐 *Deployed:* http://localhost:${CONTAINER_PORT}
⏱️ *Duration:* `${currentBuild.durationString.replace(' and counting', '')}`

🔗 [View Build](${env.BUILD_URL})
🔗 [View SonarQube](${scanSonarqube.getDashboardUrl(SONAR_PROJECT_KEY)})

🎉 Application is ready for testing!
"""
                
                sendTelegram(message, "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
            }
        }
        
        failure {
            script {
                def message = """
❌ *BUILD FAILED*

📦 *Project:* `${env.JOB_NAME}`
🔢 *Build:* `#${env.BUILD_NUMBER}`
⏱️ *Duration:* `${currentBuild.durationString.replace(' and counting', '')}`

🔗 [View Build](${env.BUILD_URL})
🔗 [View Console](${env.BUILD_URL}console)

⚠️ Please check the logs and fix the issues.
"""
                
                sendTelegram(message, "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
            }
        }
        
        always {
            script {
                // Clean up workspace
                echo "Cleaning up Docker images..."
                sh """
                    docker image prune -f
                """
            }
        }
    }
}
