@Library('devops-shared-library@main') _

/**
 * Complete CI/CD Pipeline for Spring Boot Application
 * 
 * This pipeline demonstrates:
 * - Using shared library functions for Spring Boot
 * - Building with Gradle and Dockerfile from library resources
 * - SonarQube code quality scanning with Gradle plugin
 * - Docker image build and push
 * - Container deployment with environment variables
 * - Telegram notifications with detailed status
 */

pipeline {
    agent any
    
    environment {
        // Docker Configuration
        DOCKER_REGISTRY = "docker.io"
        DOCKER_USER = "chanserey"
        IMAGE_NAME = "${DOCKER_USER}/my-spring-app"
        IMAGE_TAG = "v2.0.${env.BUILD_NUMBER}"
        FULL_IMAGE = "${IMAGE_NAME}:${IMAGE_TAG}"
        
        // Deployment Configuration
        CONTAINER_NAME = "spring-app-prod"
        CONTAINER_PORT = "8080"
        
        // Spring Boot Configuration
        SPRING_PROFILE = "production"
        
        // SonarQube Configuration
        SONAR_PROJECT_NAME = "My Spring Boot App"
        SONAR_PROJECT_KEY = "my-spring-app"
        SONAR_PROJECT_VERSION = "2.0.${env.BUILD_NUMBER}"
        
        // Telegram Configuration
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    tools {
        // Gradle tool configured in Jenkins
        gradle 'gradle-8'
    }
    
    stages {
        stage('📦 Checkout') {
            steps {
                script {
                    echo "Checking out code from repository..."
                    git branch: 'main', 
                        url: 'https://github.com/your-username/spring-boot-app.git'
                    
                    sendTelegram(
                        "🚀 *Spring Boot Build Started*\n\nProject: `${env.JOB_NAME}`\nBuild: `#${env.BUILD_NUMBER}`",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
        
        stage('🧪 Unit Tests') {
            steps {
                script {
                    echo "Running unit tests..."
                    
                    try {
                        sh './gradlew test'
                        
                        // Publish test results
                        junit '**/build/test-results/test/*.xml'
                        
                        sendTelegram(
                            "✅ Unit tests passed",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        
                    } catch (Exception e) {
                        sendTelegram(
                            "❌ Unit tests failed - check the reports",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
        
        stage('🔍 SonarQube Analysis') {
            steps {
                script {
                    echo "Running SonarQube code quality scan..."
                    
                    try {
                        // Scan Spring Boot project with SonarQube
                        // This will use Gradle sonarqube plugin if available
                        scanSonarqube.spring(
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
                        "✅ Quality Gate Passed - Code quality meets standards!",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
        
        stage('📦 Build JAR') {
            steps {
                script {
                    echo "Building Spring Boot JAR with Gradle..."
                    
                    try {
                        sh './gradlew clean build -x test'
                        
                        // Archive the JAR file
                        archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                        
                        sendTelegram(
                            "📦 JAR file built successfully",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        
                    } catch (Exception e) {
                        sendTelegram(
                            "❌ JAR build failed",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        throw e
                    }
                }
            }
        }
        
        stage('🔨 Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image using shared library Dockerfile..."
                    
                    try {
                        // Build Spring Boot image using Dockerfile from library resources
                        buildDockerImage.spring(
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
                    echo "Deploying Spring Boot container to Jenkins server..."
                    
                    try {
                        // Deploy Spring Boot container with environment variables
                        deployContainer.spring(
                            "${FULL_IMAGE}",
                            "${CONTAINER_NAME}",
                            "${CONTAINER_PORT}",
                            [
                                SPRING_PROFILES_ACTIVE: "${SPRING_PROFILE}",
                                JAVA_OPTS: "-Xms512m -Xmx1024m",
                                TZ: "Asia/Phnom_Penh"
                            ]
                        )
                        
                        // Wait for Spring Boot to start
                        sleep 30
                        
                        // Health check using Spring Boot Actuator
                        def isHealthy = deployContainer.healthCheck(
                            "${CONTAINER_NAME}",
                            "http://localhost:${CONTAINER_PORT}/actuator/health",
                            15,
                            10
                        )
                        
                        if (isHealthy) {
                            sendTelegram(
                                "🚢 Spring Boot application deployed successfully!\n\nContainer: `${CONTAINER_NAME}`\nPort: `${CONTAINER_PORT}`\nImage: `${FULL_IMAGE}`\nProfile: `${SPRING_PROFILE}`",
                                "${TELEGRAM_TOKEN}",
                                "${TELEGRAM_CHAT_ID}"
                            )
                        } else {
                            error("Container health check failed")
                        }
                        
                    } catch (Exception e) {
                        deployContainer.getLogs("${CONTAINER_NAME}", 100)
                        
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
        
        stage('🧪 Smoke Tests') {
            steps {
                script {
                    echo "Running smoke tests..."
                    
                    try {
                        // Test application endpoints
                        sh """
                            curl -f http://localhost:${CONTAINER_PORT}/actuator/health || exit 1
                            curl -f http://localhost:${CONTAINER_PORT}/actuator/info || exit 1
                        """
                        
                        sendTelegram(
                            "✅ Smoke tests passed - Application is responding",
                            "${TELEGRAM_TOKEN}",
                            "${TELEGRAM_CHAT_ID}"
                        )
                        
                    } catch (Exception e) {
                        sendTelegram(
                            "❌ Smoke tests failed - Application may not be working correctly",
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
✅ *SPRING BOOT BUILD SUCCESS*

📦 *Project:* `${env.JOB_NAME}`
🔢 *Build:* `#${env.BUILD_NUMBER}`
🏷️ *Image:* `${FULL_IMAGE}`
🌐 *API:* http://localhost:${CONTAINER_PORT}
🏥 *Health:* http://localhost:${CONTAINER_PORT}/actuator/health
⏱️ *Duration:* `${currentBuild.durationString.replace(' and counting', '')}`

🔗 [View Build](${env.BUILD_URL})
🔗 [View SonarQube](${scanSonarqube.getDashboardUrl(SONAR_PROJECT_KEY)})
🔗 [View Tests](${env.BUILD_URL}testReport)

🎉 Application is ready for testing!
"""
                
                sendTelegram(message, "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
            }
        }
        
        failure {
            script {
                def message = """
❌ *SPRING BOOT BUILD FAILED*

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
                echo "Cleaning up..."
                sh """
                    docker image prune -f
                    ./gradlew clean || true
                """
            }
        }
    }
}
