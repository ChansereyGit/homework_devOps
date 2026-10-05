#!/usr/bin/env groovy

/**
 * Deploy container on Jenkins machine or remote host
 * 
 * @param imageName The full Docker image name with tag
 * @param containerName The name for the container
 * @param port The port mapping (e.g., "8080:80" or just "80")
 * @param envVars Environment variables as a map
 * @param volumes Volume mappings as a list (e.g., ["/host/path:/container/path"])
 * @param network Docker network name
 * 
 * @example
 * deployContainer("my-app:v1.0.0", "my-app-prod", "8080:80")
 * 
 * @example with environment variables and volumes
 * deployContainer(
 *     "my-app:v1.0.0", 
 *     "my-app-prod", 
 *     "8080:80",
 *     [DATABASE_URL: "postgres://db:5432", API_KEY: "secret"],
 *     ["/var/app/data:/data"]
 * )
 */
def call(String imageName, String containerName, String port, Map envVars = [:], List volumes = [], String network = "") {
    try {
        echo "🚢 Deploying container: ${containerName}"
        echo "📦 Using image: ${imageName}"
        
        // Stop and remove existing container if it exists
        sh """
            docker stop ${containerName} 2>/dev/null || true
            docker rm ${containerName} 2>/dev/null || true
        """
        
        // Build docker run command
        def dockerCmd = "docker run -d --name ${containerName}"
        
        // Add port mapping
        if (port.contains(':')) {
            dockerCmd += " -p ${port}"
        } else {
            dockerCmd += " -p ${port}:${port}"
        }
        
        // Add environment variables
        envVars.each { key, value ->
            dockerCmd += " -e ${key}='${value}'"
        }
        
        // Add volumes
        volumes.each { volume ->
            dockerCmd += " -v ${volume}"
        }
        
        // Add network
        if (network) {
            dockerCmd += " --network ${network}"
        }
        
        // Add restart policy
        dockerCmd += " --restart unless-stopped"
        
        // Add image name
        dockerCmd += " ${imageName}"
        
        // Run the container
        sh dockerCmd
        
        // Wait for container to be healthy
        sleep 5
        
        // Check if container is running
        def isRunning = sh(
            script: "docker ps -q -f name=${containerName}",
            returnStdout: true
        ).trim()
        
        if (isRunning) {
            echo "✅ Container deployed successfully: ${containerName}"
            
            // Show container info
            sh "docker ps -f name=${containerName}"
            
            return true
        } else {
            echo "❌ Container failed to start"
            sh "docker logs ${containerName}"
            throw new Exception("Container deployment failed")
        }
        
    } catch (Exception e) {
        echo "❌ Failed to deploy container: ${e.getMessage()}"
        throw e
    }
}

/**
 * Deploy ReactJS application
 * 
 * @param imageName The full Docker image name with tag
 * @param containerName The name for the container
 * @param port The host port to expose (default: 80)
 */
def reactjs(String imageName, String containerName, String port = "80") {
    return call(imageName, containerName, "${port}:80")
}

/**
 * Deploy Spring Boot application
 * 
 * @param imageName The full Docker image name with tag
 * @param containerName The name for the container
 * @param port The host port to expose (default: 8080)
 * @param envVars Environment variables as a map
 */
def spring(String imageName, String containerName, String port = "8080", Map envVars = [:]) {
    // Add common Spring Boot environment variables
    def springEnvVars = envVars + [
        SPRING_OUTPUT_ANSI_ENABLED: "ALWAYS"
    ]
    
    return call(imageName, containerName, "${port}:8080", springEnvVars)
}

/**
 * Deploy with Docker Compose
 * 
 * @param composeFile Path to docker-compose.yml file
 * @param projectName Docker Compose project name
 * @param envFile Path to .env file (optional)
 */
def withCompose(String composeFile = "docker-compose.yml", String projectName = "app", String envFile = "") {
    try {
        echo "🚢 Deploying with Docker Compose"
        
        def composeCmd = "docker-compose -f ${composeFile} -p ${projectName}"
        
        if (envFile) {
            composeCmd += " --env-file ${envFile}"
        }
        
        // Stop existing services
        sh "${composeCmd} down"
        
        // Start services
        sh "${composeCmd} up -d"
        
        // Wait for services to be ready
        sleep 10
        
        // Show running services
        sh "${composeCmd} ps"
        
        echo "✅ Services deployed successfully with Docker Compose"
        return true
        
    } catch (Exception e) {
        echo "❌ Docker Compose deployment failed: ${e.getMessage()}"
        throw e
    }
}

/**
 * Get container logs
 * 
 * @param containerName The name of the container
 * @param lines Number of log lines to show (default: 50)
 */
def getLogs(String containerName, int lines = 50) {
    echo "📋 Showing logs for: ${containerName}"
    sh "docker logs --tail ${lines} ${containerName}"
}

/**
 * Get container status
 * 
 * @param containerName The name of the container
 * @return Container status (running, exited, etc.)
 */
def getStatus(String containerName) {
    def status = sh(
        script: "docker inspect -f '{{.State.Status}}' ${containerName} 2>/dev/null || echo 'not-found'",
        returnStdout: true
    ).trim()
    
    return status
}

/**
 * Health check for deployed container
 * 
 * @param containerName The name of the container
 * @param healthEndpoint HTTP endpoint to check
 * @param maxRetries Maximum number of retries (default: 10)
 * @param retryDelay Delay between retries in seconds (default: 5)
 */
def healthCheck(String containerName, String healthEndpoint = "", int maxRetries = 10, int retryDelay = 5) {
    echo "🏥 Performing health check for: ${containerName}"
    
    // First check if container is running
    def status = getStatus(containerName)
    
    if (status != "running") {
        echo "❌ Container is not running. Status: ${status}"
        return false
    }
    
    // If health endpoint provided, check it
    if (healthEndpoint) {
        def healthy = false
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                def response = sh(
                    script: "curl -f -s -o /dev/null -w '%{http_code}' ${healthEndpoint}",
                    returnStdout: true
                ).trim()
                
                if (response == "200") {
                    healthy = true
                    break
                }
                
                echo "⏳ Waiting for service to be healthy... (${i+1}/${maxRetries})"
                sleep retryDelay
                
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    echo "❌ Health check failed after ${maxRetries} retries"
                    getLogs(containerName, 20)
                    return false
                }
                sleep retryDelay
            }
        }
        
        if (healthy) {
            echo "✅ Container is healthy"
            return true
        }
    } else {
        echo "✅ Container is running"
        return true
    }
    
    return false
}

/**
 * Rollback deployment (stop current and start previous container)
 * 
 * @param containerName The name of the current container
 * @param previousImageName The previous image to rollback to
 */
def rollback(String containerName, String previousImageName) {
    echo "🔄 Rolling back container: ${containerName}"
    
    try {
        // Stop current container
        sh "docker stop ${containerName}"
        sh "docker rm ${containerName}"
        
        // Get port mapping from backup if available
        echo "⚠️ Manual rollback - please redeploy with previous image: ${previousImageName}"
        
        return true
    } catch (Exception e) {
        echo "❌ Rollback failed: ${e.getMessage()}"
        throw e
    }
}
