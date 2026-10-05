#!/usr/bin/env groovy

/**
 * Push Docker image to registry
 * 
 * @param imageName The full image name with tag
 * @param credentialsId Jenkins credentials ID for Docker registry
 * @param registry The Docker registry URL (default: Docker Hub)
 * 
 * @example
 * pushDockerImage("myregistry/my-app:v1.0.0", "dockerhub-credentials")
 * 
 * @example with custom registry
 * pushDockerImage("my-app:v1.0.0", "ecr-credentials", "123456789.dkr.ecr.us-east-1.amazonaws.com")
 */
def call(String imageName, String credentialsId, String registry = "") {
    try {
        echo "🚀 Pushing Docker image: ${imageName}"
        
        withCredentials([usernamePassword(
            credentialsId: credentialsId, 
            passwordVariable: 'REGISTRY_PASSWORD', 
            usernameVariable: 'REGISTRY_USERNAME'
        )]) {
            // Login to registry
            if (registry) {
                sh """
                    echo "\${REGISTRY_PASSWORD}" | docker login ${registry} -u \${REGISTRY_USERNAME} --password-stdin
                """
            } else {
                sh """
                    echo "\${REGISTRY_PASSWORD}" | docker login -u \${REGISTRY_USERNAME} --password-stdin
                """
            }
            
            // Push the image
            sh "docker push ${imageName}"
            
            echo "✅ Successfully pushed: ${imageName}"
        }
        
        return true
        
    } catch (Exception e) {
        echo "❌ Failed to push Docker image: ${e.getMessage()}"
        throw e
    }
}

/**
 * Push multiple tags of the same image
 * 
 * @param imageBaseName The base name without tag (e.g., "myregistry/my-app")
 * @param tags List of tags to push
 * @param credentialsId Jenkins credentials ID for Docker registry
 * @param registry The Docker registry URL
 * 
 * @example
 * pushDockerImage.multiTag("myuser/my-app", ["v1.0.0", "latest"], "dockerhub-creds")
 */
def multiTag(String imageBaseName, List tags, String credentialsId, String registry = "") {
    try {
        def pushedImages = []
        
        tags.each { tag ->
            def fullImageName = "${imageBaseName}:${tag}"
            call(fullImageName, credentialsId, registry)
            pushedImages.add(fullImageName)
        }
        
        echo "✅ Successfully pushed ${pushedImages.size()} image(s)"
        return pushedImages
        
    } catch (Exception e) {
        echo "❌ Failed to push multiple tags: ${e.getMessage()}"
        throw e
    }
}

/**
 * Push to Docker Hub
 * 
 * @param imageName The full image name with tag
 * @param credentialsId Jenkins credentials ID for Docker Hub
 */
def toDockerHub(String imageName, String credentialsId) {
    return call(imageName, credentialsId)
}

/**
 * Push to AWS ECR
 * 
 * @param imageName The full image name with tag
 * @param credentialsId Jenkins credentials ID for AWS
 * @param region AWS region
 * @param accountId AWS account ID
 */
def toECR(String imageName, String credentialsId, String region, String accountId) {
    def registry = "${accountId}.dkr.ecr.${region}.amazonaws.com"
    
    // Get ECR login token
    withCredentials([usernamePassword(
        credentialsId: credentialsId,
        passwordVariable: 'AWS_SECRET_ACCESS_KEY',
        usernameVariable: 'AWS_ACCESS_KEY_ID'
    )]) {
        sh """
            aws ecr get-login-password --region ${region} | \
            docker login --username AWS --password-stdin ${registry}
        """
    }
    
    return call(imageName, credentialsId, registry)
}

/**
 * Push to Google Container Registry (GCR)
 * 
 * @param imageName The full image name with tag
 * @param credentialsId Jenkins credentials ID for GCP
 * @param project GCP project ID
 */
def toGCR(String imageName, String credentialsId, String project) {
    def registry = "gcr.io/${project}"
    
    withCredentials([file(credentialsId: credentialsId, variable: 'GCP_KEY')]) {
        sh """
            cat \${GCP_KEY} | docker login -u _json_key --password-stdin https://gcr.io
        """
    }
    
    return call(imageName, credentialsId, registry)
}

/**
 * Verify image exists in registry
 * 
 * @param imageName The full image name with tag
 * @param credentialsId Jenkins credentials ID
 * @param registry The Docker registry URL
 * @return true if image exists, false otherwise
 */
def verifyImage(String imageName, String credentialsId, String registry = "") {
    try {
        withCredentials([usernamePassword(
            credentialsId: credentialsId,
            passwordVariable: 'REGISTRY_PASSWORD',
            usernameVariable: 'REGISTRY_USERNAME'
        )]) {
            if (registry) {
                sh """
                    echo "\${REGISTRY_PASSWORD}" | docker login ${registry} -u \${REGISTRY_USERNAME} --password-stdin
                """
            } else {
                sh """
                    echo "\${REGISTRY_PASSWORD}" | docker login -u \${REGISTRY_USERNAME} --password-stdin
                """
            }
            
            sh "docker manifest inspect ${imageName} > /dev/null 2>&1"
            echo "✅ Image verified in registry: ${imageName}"
            return true
        }
    } catch (Exception e) {
        echo "❌ Image not found in registry: ${imageName}"
        return false
    }
}
