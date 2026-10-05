#!/usr/bin/env groovy

/**
 * Build Docker image using Dockerfile from shared library resources
 * 
 * @param imageName The name of the Docker image (e.g., "myapp")
 * @param tag The tag for the image (default: BUILD_NUMBER)
 * @param dockerfileType The type of Dockerfile to use (reactjs, spring)
 * @param contextPath The build context path (default: .)
 * @param buildArgs Additional build arguments as a map
 * 
 * @example
 * buildDockerImage("my-react-app", "v1.0.0", "reactjs")
 * 
 * @example with build args
 * buildDockerImage("my-spring-app", "latest", "spring", ".", [NODE_ENV: "production"])
 */
def call(String imageName, String tag = "${env.BUILD_NUMBER}", String dockerfileType = "reactjs", String contextPath = ".", Map buildArgs = [:]) {
    try {
        // Get the Dockerfile from library resources
        def dockerfileContent = libraryResource "${dockerfileType}.Dockerfile"
        
        // Write the Dockerfile to workspace
        writeFile file: "Dockerfile.${dockerfileType}", text: dockerfileContent
        
        // Get nginx.conf if it's a ReactJS build
        if (dockerfileType == "reactjs") {
            def nginxConfig = libraryResource "nginx.conf"
            writeFile file: "nginx.conf", text: nginxConfig
        }
        
        // Build the image with the full tag
        def fullImageName = "${imageName}:${tag}"
        
        // Prepare build args
        def buildArgsStr = ""
        if (buildArgs) {
            buildArgs.each { key, value ->
                buildArgsStr += "--build-arg ${key}=${value} "
            }
        }
        
        echo "🔨 Building Docker image: ${fullImageName}"
        echo "📋 Using Dockerfile type: ${dockerfileType}"
        
        sh """
            docker build ${buildArgsStr} \
                -t ${fullImageName} \
                -f Dockerfile.${dockerfileType} \
                ${contextPath}
        """
        
        echo "✅ Docker image built successfully: ${fullImageName}"
        
        // Return the full image name for use in subsequent stages
        return fullImageName
        
    } catch (Exception e) {
        echo "❌ Docker build failed: ${e.getMessage()}"
        throw e
    }
}

/**
 * Build ReactJS Docker image using the shared library Dockerfile
 * 
 * @param imageName The name of the Docker image
 * @param tag The tag for the image
 * @param registry Optional registry prefix (e.g., "docker.io/username")
 * 
 * @example
 * def image = buildDockerImage.reactjs("my-react-app", "v1.0.0", "myregistry")
 */
def reactjs(String imageName, String tag = "${env.BUILD_NUMBER}", String registry = "") {
    def fullImageName = registry ? "${registry}/${imageName}" : imageName
    return call(fullImageName, tag, "reactjs")
}

/**
 * Build Spring Boot Docker image using the shared library Dockerfile
 * 
 * @param imageName The name of the Docker image
 * @param tag The tag for the image
 * @param registry Optional registry prefix (e.g., "docker.io/username")
 * 
 * @example
 * def image = buildDockerImage.spring("my-spring-app", "v2.0.0", "myregistry")
 */
def spring(String imageName, String tag = "${env.BUILD_NUMBER}", String registry = "") {
    def fullImageName = registry ? "${registry}/${imageName}" : imageName
    return call(fullImageName, tag, "spring")
}

/**
 * Build and tag Docker image with multiple tags
 * 
 * @param imageName The name of the Docker image
 * @param tags List of tags to apply
 * @param dockerfileType The type of Dockerfile to use
 * 
 * @example
 * buildDockerImage.multiTag("my-app", ["v1.0.0", "latest", "prod"], "reactjs")
 */
def multiTag(String imageName, List tags, String dockerfileType = "reactjs") {
    // Build with first tag
    def primaryImage = call(imageName, tags[0], dockerfileType)
    
    // Tag with additional tags
    tags.drop(1).each { tag ->
        def additionalTag = "${imageName}:${tag}"
        sh "docker tag ${primaryImage} ${additionalTag}"
        echo "🏷️ Tagged image as: ${additionalTag}"
    }
    
    return tags.collect { "${imageName}:${it}" }
}

/**
 * Get Docker image size
 * 
 * @param imageName Full image name with tag
 * @return Image size as string
 */
def getImageSize(String imageName) {
    def size = sh(script: "docker images ${imageName} --format '{{.Size}}'", returnStdout: true).trim()
    return size
}

/**
 * List all layers of the Docker image
 * 
 * @param imageName Full image name with tag
 */
def showImageHistory(String imageName) {
    echo "📜 Image history for: ${imageName}"
    sh "docker history ${imageName}"
}
