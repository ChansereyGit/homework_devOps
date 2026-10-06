def call(String message, String token, String chatId){
    sh """
        curl -s -X POST "https://api.telegram.org/bot${token}/sendMessage" \\
            -d chat_id="${chatId}" \\
            -d parse_mode="Markdown" \\
            -d text="${message}"
    """
}
