#!/usr/bin/env groovy

/**
 * Send a message to Telegram
 * 
 * @param message The message to send (supports Markdown formatting)
 * @param token The Telegram bot token
 * @param chatId The Telegram chat ID
 * @param parseMode The parse mode (default: Markdown, options: Markdown, HTML, MarkdownV2)
 * 
 * @example
 * sendTelegram("Hello from Jenkins!", "${env.TELEGRAM_TOKEN}", "${env.TELEGRAM_CHAT_ID}")
 * 
 * @example with markdown
 * def message = """
 * *Build Success* ✅
 * 
 * Project: `${env.JOB_NAME}`
 * Build: `#${env.BUILD_NUMBER}`
 * Status: *SUCCESS*
 * 
 * [View Build](${env.BUILD_URL})
 * """
 * sendTelegram(message, "${TOKEN}", "${CHAT_ID}")
 */
def call(String message, String token, String chatId, String parseMode = "Markdown") {
    try {
        // Escape special characters for shell
        def escapedMessage = message.replace('"', '\\"').replace('$', '\\$').replace('`', '\\`')
        
        sh """
            curl -s -X POST "https://api.telegram.org/bot${token}/sendMessage" \
                -d chat_id="${chatId}" \
                -d parse_mode="${parseMode}" \
                -d text="${escapedMessage}" > /dev/null
        """
        
        echo "✅ Telegram message sent successfully to chat ID: ${chatId}"
        return true
        
    } catch (Exception e) {
        echo "❌ Failed to send Telegram message: ${e.getMessage()}"
        return false
    }
}

/**
 * Send a build notification to Telegram with status emoji
 * 
 * @param status The build status (SUCCESS, FAILURE, UNSTABLE, ABORTED)
 * @param token The Telegram bot token
 * @param chatId The Telegram chat ID
 * 
 * @example
 * sendTelegram.buildNotification("SUCCESS", "${env.TELEGRAM_TOKEN}", "${env.TELEGRAM_CHAT_ID}")
 */
def buildNotification(String status, String token, String chatId) {
    def emoji = getStatusEmoji(status)
    def message = """
${emoji} *BUILD ${status}*

📦 *Project:* `${env.JOB_NAME}`
🔢 *Build:* `#${env.BUILD_NUMBER}`
🌿 *Branch:* `${env.GIT_BRANCH ?: 'N/A'}`
⏱️ *Duration:* `${currentBuild.durationString?.replace(' and counting', '')}`

🔗 [View Build](${env.BUILD_URL})
👤 *Started by:* ${env.BUILD_USER ?: 'Jenkins'}
"""
    
    call(message, token, chatId)
}

/**
 * Send a stage notification to Telegram
 * 
 * @param stageName The name of the stage
 * @param status The stage status
 * @param token The Telegram bot token
 * @param chatId The Telegram chat ID
 */
def stageNotification(String stageName, String status, String token, String chatId) {
    def emoji = getStatusEmoji(status)
    def message = """
${emoji} *Stage: ${stageName}*

Status: *${status}*
Build: `#${env.BUILD_NUMBER}`
Project: `${env.JOB_NAME}`
"""
    
    call(message, token, chatId)
}

/**
 * Get emoji for build status
 */
private def getStatusEmoji(String status) {
    switch(status?.toUpperCase()) {
        case 'SUCCESS':
            return '✅'
        case 'FAILURE':
            return '❌'
        case 'UNSTABLE':
            return '⚠️'
        case 'ABORTED':
            return '🛑'
        default:
            return 'ℹ️'
    }
}
