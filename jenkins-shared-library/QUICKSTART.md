# Quick Start Guide

Fast setup guide with pre-configured values for this homework.

## Your Configuration

**Docker Hub:** `chanserey`  
**Telegram Bot:** `@reiJenkinsBot`  
**Bot Token:** `8905728861:AAF7U1YF5er_uPXUiYBxFQ9qAa3iv0jeVUc`

## 1. Get Your Telegram Chat ID

```bash
# Send a message to your bot first, then run:
curl https://api.telegram.org/bot8905728861:AAF7U1YF5er_uPXUiYBxFQ9qAa3iv0jeVUc/getUpdates

# Look for: "chat":{"id":123456789}
# Save this chat ID
```

## 2. Start SonarQube (Optional)

```bash
cd jenkins-shared-library
docker-compose -f docker-compose.sonarqube.yaml up -d

# Access: http://localhost:9000
# Login: admin / admin (change on first login)
```

## 3. Configure Jenkins Credentials

Go to **Manage Jenkins → Manage Credentials → (global) → Add Credentials**

### Docker Hub Credentials
- Kind: Username with password
- ID: `dockerhub-credentials`
- Username: `chanserey`
- Password: Your Docker Hub password or token

### Telegram Bot Token
- Kind: Secret text
- ID: `telegram-bot-token`
- Secret: `8905728861:AAF7U1YF5er_uPXUiYBxFQ9qAa3iv0jeVUc`

### Telegram Chat ID
- Kind: Secret text
- ID: `telegram-chat-id`
- Secret: Your chat ID from step 1

### SonarQube Token (if using)
- Kind: Secret text
- ID: `SONARQUBE-TOKEN`
- Secret: Generate token from SonarQube

## 4. Configure Shared Library in Jenkins

**Manage Jenkins → Configure System → Global Pipeline Libraries**

- Name: `devops-shared-library`
- Default version: `main`
- Retrieval: Modern SCM → Git
- Repository: `https://github.com/ChansereyGit/homework_devOps.git`
- Library Path: `jenkins-shared-library` (if library is in subfolder)

## 5. Test with Simple Pipeline

Create new Pipeline job and paste:

```groovy
@Library('devops-shared-library@main') _

pipeline {
    agent any
    
    environment {
        TELEGRAM_TOKEN = credentials('telegram-bot-token')
        TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
    }
    
    stages {
        stage('Test') {
            steps {
                script {
                    sendTelegram(
                        "✅ Shared library works!",
                        "${TELEGRAM_TOKEN}",
                        "${TELEGRAM_CHAT_ID}"
                    )
                }
            }
        }
    }
}
```

## 6. Deploy Demo Apps

### React Demo App

```bash
cd demo-apps/react-demo-app
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/ChansereyGit/react-demo-app.git
git push -u origin main
```

Then create Pipeline in Jenkins with Jenkinsfile from repo.

### Spring Demo App

```bash
cd demo-apps/spring-demo-app
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/ChansereyGit/spring-demo-app.git
git push -u origin main
```

Then create Pipeline in Jenkins with Jenkinsfile from repo.

## Next Steps

1. ✅ Push this library to GitHub
2. ✅ Configure Jenkins with above credentials
3. ✅ Test Telegram bot
4. ✅ Run demo app pipelines
5. ✅ Customize for your projects

## Troubleshooting

**Can't receive Telegram messages?**
- Verify bot token is correct
- Check chat ID is correct
- Ensure you sent a message to the bot first

**Docker permission denied?**
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

**Library not found?**
- Check library name: `devops-shared-library`
- Verify repository URL
- Check branch: `main`

For detailed setup, see [SETUP.md](SETUP.md)
