def call(String imageName, String tag, String dockerfileType){
    def dockerfileContent = libraryResource "${dockerfileType}.Dockerfile"
    writeFile file: "Dockerfile", text: dockerfileContent
    
    if(dockerfileType == "reactjs"){
        def nginxConfig = libraryResource "nginx.conf"
        writeFile file: "nginx.conf", text: nginxConfig
    }
    
    sh """
        docker build -t ${imageName}:${tag} .
    """
}
