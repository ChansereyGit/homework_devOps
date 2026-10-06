def call(String imageName, String tag, String containerName, String port){
    sh """
        docker stop ${containerName} || true
        docker rm ${containerName} || true
        docker run -dp ${port}:80 --name ${containerName} ${imageName}:${tag}
    """
}
