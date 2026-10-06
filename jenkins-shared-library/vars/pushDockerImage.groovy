def call(String imageName, String tag, String credentialsId){
    withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
        sh """
            echo "${PASSWORD}" | docker login -u ${USERNAME} --password-stdin
            docker push ${imageName}:${tag}
        """
    }
}
