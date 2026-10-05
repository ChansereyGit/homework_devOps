# Jenkins Shared Library Setup Guide

Complete step-by-step guide to configure and use this Jenkins Shared Library.

Complete step-by-step guide to configure and use the Jenkins Shared Library.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Jenkins Setup](#jenkins-setup)
3. [Configure Shared Library](#configure-shared-library)
4. [Install Required Plugins](#install-required-plugins)
5. [Configure Tools](#configure-tools)
6. [Configure Credentials](#configure-credentials)
7. [Configure SonarQube](#configure-sonarqube)
8. [Setup Telegram Bot](#setup-telegram-bot)
9. [Create Your First Pipeline](#create-your-first-pipeline)
10. [Testing the Library](#testing-the-library)
11. [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

Before starting, ensure you have:

- ✅ Jenkins 2.400+ installed and running
- ✅ Docker installed on Jenkins server
- ✅ Git installed
- ✅ Network access to Docker Hub (or your registry)
- ✅ SonarQube server (optional but recommended)
- ✅ Telegram account (optional for notifications)

---

## 🚀 Jenkins Setup

### Step 1: Install Jenkins

**On Ubuntu/Debian:**

```bash
# Update system
sudo apt update

# Install Java
sudo apt install fontconfig openjdk-21-jre -y

# Add Jenkins repository
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | \
  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# Install Jenkins
sudo apt update
sudo apt install jenkins -y

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
```

### Step 2: Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add Jenkins user to docker group
sudo usermod -aG docker jenkins

# Restart Jenkins
sudo systemctl restart jenkins
```

### Step 3: Initial Jenkins Configuration

1. Access Jenkins: `http://your-server:8080`
2. Get initial admin password:
   ```bash
   sudo cat /var/lib/jenkins/secrets/initialAdminPassword
   ```
3. Install suggested plugins
4. Create admin user
5. Configure Jenkins URL

---

## 📚 Configure Shared Library

### Option 1: Global Trusted Library (Recommended)

1. **Navigate to Global Configuration**
   - Go to: **Manage Jenkins** → **Configure System**
   - Scroll to **Global Pipeline Libraries** section

2. **Add Library**
   - Click **Add** button
   
3. **Configure Library**
   - **Name**: `devops-shared-library`
   - **Default version**: `main` (or `master`)
   - **Load implicitly**: ❌ Unchecked (explicit loading recommended)
   - **Allow default version to be overridden**: ✅ Checked
   - **Include @Library changes in job recent changes**: ✅ Checked

4. **Configure Retrieval Method**
   - **Retrieval method**: Modern SCM
   
5. **Configure Source Code Management**
   - **Source Code Management**: Git
   - **Project Repository**: 
     ```
     https://github.com/your-org/jenkins-shared-library.git
     ```
   - **Credentials**: Add if private repository
   - **Behaviors**: Default

6. **Save Configuration**

### Option 2: Folder-Level Library

1. Create a folder in Jenkins
2. Configure folder properties
3. In **Pipeline Libraries** section, add library configuration
4. All jobs in this folder will have access to the library

### Option 3: Project-Level Library

Add to your Jenkinsfile:

```groovy
@Library('devops-shared-library@main') _

// OR with specific version
@Library('devops-shared-library@v1.0.0') _
```

---

## 🔌 Install Required Plugins

### Step 1: Navigate to Plugin Manager

Go to: **Manage Jenkins** → **Manage Plugins** → **Available**

### Step 2: Install These Plugins

**Essential Plugins:**
- ✅ **Pipeline** (usually pre-installed)
- ✅ **Pipeline: Groovy**
- ✅ **Git Plugin**
- ✅ **Docker Pipeline**
- ✅ **Docker Commons Plugin**
- ✅ **Credentials Plugin**
- ✅ **Credentials Binding Plugin**

**For SonarQube:**
- ✅ **SonarQube Scanner**

**For Notifications:**
- ✅ **HTTP Request Plugin** (for Telegram)

**Optional but Recommended:**
- ✅ **Blue Ocean** (better UI)
- ✅ **Pipeline Stage View**
- ✅ **Build Timestamp**
- ✅ **Timestamper**

### Step 3: Restart Jenkins

After installing plugins:
```bash
sudo systemctl restart jenkins
```

---

## 🛠️ Configure Tools

### Configure SonarQube Scanner

1. **Navigate to Tools Configuration**
   - Go to: **Manage Jenkins** → **Global Tool Configuration**

2. **Add SonarQube Scanner**
   - Scroll to **SonarQube Scanner** section
   - Click **Add SonarQube Scanner**
   
3. **Configure Scanner**
   - **Name**: `sonar-scanner` (must match this name!)
   - **Install automatically**: ✅ Checked
   - **Version**: Select latest version (e.g., SonarQube Scanner 5.0.1)

4. **Save Configuration**

### Configure Gradle (If using Spring Boot)

1. In **Global Tool Configuration**
2. Scroll to **Gradle** section
3. Click **Add Gradle**
4. Configure:
   - **Name**: `gradle-8`
   - **Install automatically**: ✅ Checked
   - **Version**: Gradle 8.5 (or latest)

### Configure Maven (If needed)

1. In **Global Tool Configuration**
2. Scroll to **Maven** section
3. Click **Add Maven**
4. Configure:
   - **Name**: `maven-3`
   - **Install automatically**: ✅ Checked
   - **Version**: Maven 3.9.5 (or latest)

---

## 🔐 Configure Credentials

### 1. Docker Hub Credentials

1. **Navigate to Credentials**
   - Go to: **Manage Jenkins** → **Manage Credentials**
   - Click on **(global)** domain
   - Click **Add Credentials**

2. **Configure Credential**
   - **Kind**: Username with password
   - **Scope**: Global
   - **Username**: Your Docker Hub username
   - **Password**: Your Docker Hub password or access token
   - **ID**: `dockerhub-credentials` (must match!)
   - **Description**: Docker Hub Credentials

3. **Create Access Token** (Recommended)
   - Go to: https://hub.docker.com/settings/security
   - Click **New Access Token**
   - Give it a name: `jenkins-ci`
   - Copy the token
   - Use token as password in Jenkins

### 2. SonarQube Token

1. **Generate Token in SonarQube**
   - Log in to SonarQube
   - Go to: **My Account** → **Security** → **Generate Tokens**
   - Token name: `jenkins`
   - Type: Global Analysis Token
   - Click **Generate**
   - Copy the token immediately!

2. **Add to Jenkins**
   - Go to: **Manage Jenkins** → **Manage Credentials**
   - Click **Add Credentials**
   - **Kind**: Secret text
   - **Scope**: Global
   - **Secret**: Paste your SonarQube token
   - **ID**: `SONARQUBE-TOKEN` (must match!)
   - **Description**: SonarQube Authentication Token

### 3. Telegram Credentials

1. **Add Bot Token**
   - **Kind**: Secret text
   - **Secret**: Your Telegram bot token (e.g., `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)
   - **ID**: `telegram-bot-token`
   - **Description**: Telegram Bot Token

2. **Add Chat ID**
   - **Kind**: Secret text
   - **Secret**: Your Telegram chat ID (e.g., `-1001234567890`)
   - **ID**: `telegram-chat-id`
   - **Description**: Telegram Chat ID

---

## 🔍 Configure SonarQube

### Step 1: Install SonarQube (If not already installed)

**Using Docker:**

```bash
docker run -d --name sonarqube \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  sonarqube:lts-community
```

**Access**: http://your-server:9000
- Default credentials: `admin` / `admin`
- Change password on first login

### Step 2: Configure SonarQube in Jenkins

1. **Navigate to System Configuration**
   - Go to: **Manage Jenkins** → **Configure System**
   - Scroll to **SonarQube servers** section

2. **Add SonarQube Server**
   - Click **Add SonarQube**
   
3. **Configure Server**
   - **Name**: `sonar-scanner` (must match!)
   - **Server URL**: `http://your-sonarqube-server:9000`
   - **Server authentication token**: Select `SONARQUBE-TOKEN` credential
   
4. **Save Configuration**

### Step 3: Configure Quality Gate in SonarQube

1. Log in to SonarQube
2. Go to: **Quality Gates**
3. Create a new quality gate or use "Sonar way" (default)
4. Set as default

### Step 4: Configure Webhook (For Quality Gate)

1. In SonarQube, go to: **Administration** → **Configuration** → **Webhooks**
2. Click **Create**
3. **Name**: Jenkins
4. **URL**: `http://your-jenkins-server:8080/sonarqube-webhook/`
5. **Secret**: Leave empty or set if needed
6. Click **Create**

---

## 📱 Setup Telegram Bot

### Step 1: Create Telegram Bot

1. Open Telegram and search for `@BotFather`
2. Send command: `/newbot`
3. Follow instructions:
   - Choose a name: `My Jenkins Bot`
   - Choose a username: `my_jenkins_bot`
4. Copy the bot token (e.g., `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)

### Step 2: Get Chat ID

**Option 1: Using a Group/Channel**

1. Create a group or channel in Telegram
2. Add your bot to the group/channel
3. Send a message in the group
4. Visit: `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
5. Look for `"chat":{"id":-1001234567890}` in the response
6. Copy the chat ID (including the minus sign if present)

**Option 2: Using Private Chat**

1. Send a message to your bot
2. Visit: `https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates`
3. Look for your message and find the chat ID
4. Copy the chat ID

### Step 3: Test Telegram Integration

```bash
# Test sending a message
curl -X POST "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/sendMessage" \
  -d chat_id="<YOUR_CHAT_ID>" \
  -d text="Test message from Jenkins setup"
```

If you receive the message, your setup is correct!

---

## 🎯 Create Your First Pipeline

### Step 1: Create a New Pipeline Job

1. **Create Job**
   - Click **New Item**
   - Enter name: `test-shared-library`
   - Select **Pipeline**
   - Click **OK**

### Step 2: Configure Pipeline

In the **Pipeline** section:

**Option A: Pipeline Script**

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    environment {
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    stages {
        stage('Test Telegram') {
            steps {
                script {
                    sendTelegram(
                        "🎉 *Shared Library Works!*\n\nJenkins shared library is configured correctly.",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
        
        stage('Test Docker Build') {
            steps {
                script {
                    // Create a simple test file
                    writeFile file: 'package.json', text: '''
                    {
                      "name": "test-app",
                      "version": "1.0.0",
                      "scripts": {
                        "build": "echo 'Building...'"
                      }
                    }
                    '''
                    
                    // This will test if Dockerfile can be loaded
                    echo "Testing Dockerfile resource loading..."
                    def dockerfile = libraryResource 'reactjs.Dockerfile'
                    echo "✅ Dockerfile loaded successfully!"
                }
            }
        }
    }
    
    post {
        success {
            script {
                sendTelegram(
                    "✅ Test pipeline completed successfully!",
                    "${TELEGRAM_TOKEN}",
                    "${TELEGRAM_CHAT_ID}"
                )
            }
        }
    }
}
```

**Option B: Pipeline from SCM**

1. **Definition**: Pipeline script from SCM
2. **SCM**: Git
3. **Repository URL**: Your project repository
4. **Script Path**: `Jenkinsfile`

### Step 3: Run the Pipeline

1. Click **Build Now**
2. Watch the console output
3. Check Telegram for notification
4. Verify all stages pass ✅

---

## ✅ Testing the Library

### Test 1: Telegram Notifications

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    environment {
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    stages {
        stage('Test Notifications') {
            steps {
                script {
                    // Test basic message
                    sendTelegram("Test message", "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
                    
                    // Test build notification
                    sendTelegram.buildNotification("SUCCESS", "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
                    
                    // Test stage notification
                    sendTelegram.stageNotification("Test Stage", "SUCCESS", "${TELEGRAM_TOKEN}", "${TELEGRAM_CHAT_ID}")
                }
            }
        }
    }
}
```

### Test 2: Docker Build

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    stages {
        stage('Test Docker Build') {
            steps {
                script {
                    // Create minimal React app structure
                    sh '''
                        mkdir -p src
                        echo '{"name":"test","version":"1.0.0","scripts":{"build":"echo Building"}}' > package.json
                        echo 'console.log("test");' > src/index.js
                    '''
                    
                    // Test building with library Dockerfile
                    buildDockerImage.reactjs("test-app", "1.0.0")
                    
                    // Verify image exists
                    sh 'docker images | grep test-app'
                    
                    // Clean up
                    sh 'docker rmi test-app:1.0.0'
                }
            }
        }
    }
}
```

### Test 3: Complete Integration Test

Use the example pipelines in `examples/` folder:
- `reactjs-pipeline.groovy`
- `spring-pipeline.groovy`

---

## 🔧 Troubleshooting

### Issue: "Library not found"

**Symptoms:**
```
org.jenkinsci.plugins.workflow.cps.CpsCompilationErrorsException:
RejectedAccessException: No such property: devops-shared-library
```

**Solution:**
1. Verify library name matches exactly: `devops-shared-library`
2. Check library is configured in **Manage Jenkins** → **Configure System**
3. Verify repository URL is accessible
4. Check branch name (`main` or `master`)

### Issue: "libraryResource not found"

**Symptoms:**
```
No such resource: reactjs.Dockerfile
```

**Solution:**
1. Ensure files are in `resources/` folder (not `resources/com/devops/`)
2. File names must match exactly: `reactjs.Dockerfile`, `spring.Dockerfile`, `nginx.conf`
3. Commit and push files to Git repository
4. Refresh library in Jenkins

### Issue: Docker permission denied

**Symptoms:**
```
Got permission denied while trying to connect to the Docker daemon socket
```

**Solution:**
```bash
# Add Jenkins user to docker group
sudo usermod -aG docker jenkins

# Restart Jenkins
sudo systemctl restart jenkins

# Verify
sudo -u jenkins docker ps
```

### Issue: SonarQube scanner not found

**Symptoms:**
```
No such tool: sonar-scanner
```

**Solution:**
1. Go to: **Manage Jenkins** → **Global Tool Configuration**
2. Add **SonarQube Scanner** with name: `sonar-scanner`
3. Enable **Install automatically**
4. Save and retry

### Issue: Quality Gate webhook not working

**Symptoms:**
- Pipeline hangs at "Waiting for Quality Gate"
- Timeout after 5 minutes

**Solution:**
1. Verify webhook is configured in SonarQube:
   - URL: `http://jenkins-server:8080/sonarqube-webhook/`
2. Check Jenkins can receive webhooks (firewall rules)
3. Verify SonarQube server name matches in Jenkins
4. Test webhook manually in SonarQube

### Issue: Telegram messages not sending

**Symptoms:**
- No error but no message received
- Curl command fails

**Solution:**
1. Verify bot token is correct
2. Verify chat ID is correct (including minus sign for groups)
3. Ensure bot is member of group/channel
4. Test with curl:
```bash
curl -X POST "https://api.telegram.org/bot<TOKEN>/sendMessage" \
  -d chat_id="<CHAT_ID>" \
  -d text="Test"
```

### Issue: Container deployment fails

**Symptoms:**
- Container stops immediately after starting
- Health check fails

**Solution:**
1. Check container logs:
```groovy
deployContainer.getLogs("container-name", 100)
```
2. Verify port is not already in use
3. Check environment variables are correct
4. Increase health check retries and delay
5. Verify image was built successfully

---

## 📝 Best Practices

### 1. Version Control

Always specify library version in production:
```groovy
@Library('devops-shared-library@v1.0.0') _
```

### 2. Use Environment Variables

```groovy
environment {
    IMAGE_NAME = "${env.DOCKER_REGISTRY}/${env.PROJECT_NAME}"
    IMAGE_TAG = "v1.0.${BUILD_NUMBER}"
}
```

### 3. Secure Credentials

Never hardcode secrets:
```groovy
// ❌ Wrong
def token = "123456:ABC-DEF"

// ✅ Correct
environment {
    TOKEN = credentials('telegram-bot-token')
}
```

### 4. Clean Up Resources

```groovy
post {
    always {
        sh 'docker image prune -f'
        cleanWs()
    }
}
```

### 5. Use Try-Catch for Error Handling

```groovy
stage('Deploy') {
    steps {
        script {
            try {
                deployContainer.reactjs(...)
            } catch (Exception e) {
                sendTelegram("❌ Deployment failed: ${e.message}", ...)
                throw e
            }
        }
    }
}
```

---

## 📚 Next Steps

1. ✅ Complete this setup guide
2. ✅ Test with simple pipeline
3. ✅ Run example pipelines
4. ✅ Create your own pipeline
5. ✅ Integrate with your projects
6. ✅ Configure monitoring and alerts
7. ✅ Set up backup and disaster recovery

---

## 🎓 Learning Resources

- [Jenkins Pipeline Tutorial](https://www.jenkins.io/doc/book/pipeline/)
- [Shared Libraries Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)
- [Docker Documentation](https://docs.docker.com/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Telegram Bot API](https://core.telegram.org/bots/api)

---

## 🤝 Support

If you encounter issues:
1. Check this troubleshooting guide
2. Review Jenkins console output
3. Check Docker logs
4. Verify all credentials are configured
5. Test each component individually

---

**Setup complete! You're ready to use the shared library! 🚀**
