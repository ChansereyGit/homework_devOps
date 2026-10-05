#!/usr/bin/env groovy

/**
 * Scan project with SonarQube
 * 
 * @param projectName The name of the project
 * @param projectVersion The version of the project
 * @param projectKey The unique key for the project in SonarQube
 * @param sources The source directories to scan (default: src)
 * @param exclusions Files/directories to exclude from scan
 * 
 * @example
 * scanSonarqube("MyReactApp", "1.0.0", "my-react-app")
 * 
 * @example with custom sources
 * scanSonarqube("MySpringApp", "2.0.0", "my-spring-app", "src/main/java", "**/test/**")
 */
def call(String projectName, String projectVersion, String projectKey, String sources = "src", String exclusions = "") {
    try {
        // Get the sonar-scanner tool configured in Jenkins
        def scannerHome = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
        
        // Build the sonar-scanner command
        def scanCommand = """
            ${scannerHome}/bin/sonar-scanner \\
                -Dsonar.projectName="${projectName}" \\
                -Dsonar.projectKey=${projectKey} \\
                -Dsonar.projectVersion=${projectVersion} \\
                -Dsonar.sources=${sources}
        """
        
        // Add exclusions if provided
        if (exclusions) {
            scanCommand += " \\\n                -Dsonar.exclusions=${exclusions}"
        }
        
        // Execute within SonarQube environment
        withSonarQubeEnv(credentialsId: 'SONARQUBE-TOKEN', installationName: 'sonar-scanner') {
            sh scanCommand
        }
        
        echo "✅ SonarQube scan completed for project: ${projectName}"
        return true
        
    } catch (Exception e) {
        echo "❌ SonarQube scan failed: ${e.getMessage()}"
        throw e
    }
}

/**
 * Scan ReactJS project with SonarQube
 * 
 * @param projectName The name of the project
 * @param projectVersion The version of the project
 * @param projectKey The unique key for the project in SonarQube
 */
def reactjs(String projectName, String projectVersion, String projectKey) {
    def sources = "src"
    def exclusions = "**/node_modules/**,**/build/**,**/dist/**,**/*.test.js,**/*.spec.js"
    
    call(projectName, projectVersion, projectKey, sources, exclusions)
}

/**
 * Scan Spring Boot project with SonarQube
 * 
 * @param projectName The name of the project
 * @param projectVersion The version of the project
 * @param projectKey The unique key for the project in SonarQube
 */
def spring(String projectName, String projectVersion, String projectKey) {
    def sources = "src/main/java"
    def exclusions = "**/target/**,**/build/**,**/*Test.java,**/*Tests.java"
    
    // For Spring Boot projects with Gradle/Maven, use built-in analysis
    try {
        def scannerHome = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
        
        withSonarQubeEnv(credentialsId: 'SONARQUBE-TOKEN', installationName: 'sonar-scanner') {
            // Check if it's a Gradle or Maven project
            if (fileExists('build.gradle') || fileExists('build.gradle.kts')) {
                sh """
                    ./gradlew sonarqube \\
                        -Dsonar.projectName="${projectName}" \\
                        -Dsonar.projectKey=${projectKey} \\
                        -Dsonar.projectVersion=${projectVersion}
                """
            } else if (fileExists('pom.xml')) {
                sh """
                    mvn sonar:sonar \\
                        -Dsonar.projectName="${projectName}" \\
                        -Dsonar.projectKey=${projectKey} \\
                        -Dsonar.projectVersion=${projectVersion}
                """
            } else {
                // Fallback to sonar-scanner
                sh """
                    ${scannerHome}/bin/sonar-scanner \\
                        -Dsonar.projectName="${projectName}" \\
                        -Dsonar.projectKey=${projectKey} \\
                        -Dsonar.projectVersion=${projectVersion} \\
                        -Dsonar.sources=${sources} \\
                        -Dsonar.exclusions=${exclusions} \\
                        -Dsonar.java.binaries=build/classes
                """
            }
        }
        
        echo "✅ SonarQube scan completed for Spring project: ${projectName}"
        return true
        
    } catch (Exception e) {
        echo "❌ SonarQube scan failed: ${e.getMessage()}"
        throw e
    }
}

/**
 * Wait for SonarQube Quality Gate result
 * 
 * @param timeoutMinutes Maximum time to wait for quality gate (default: 5 minutes)
 * @return Quality gate status object
 * 
 * @example
 * def qg = scanSonarqube.waitForQualityGate()
 * if (qg.status != 'OK') {
 *     error("Quality Gate failed: ${qg.status}")
 * }
 */
def waitForQualityGate(int timeoutMinutes = 5) {
    try {
        timeout(time: timeoutMinutes, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            
            echo """
╔════════════════════════════════════════╗
║     SonarQube Quality Gate Result      ║
╠════════════════════════════════════════╣
║ Status: ${qg.status.padRight(31)}║
╚════════════════════════════════════════╝
"""
            
            return qg
        }
    } catch (Exception e) {
        echo "❌ Quality Gate check failed: ${e.getMessage()}"
        throw e
    }
}

/**
 * Get SonarQube dashboard URL for the project
 */
def getDashboardUrl(String projectKey) {
    return "${env.SONAR_HOST_URL}/dashboard?id=${projectKey}"
}
