def call(String projectName, String projectVersion, String projectKey){
    def scannerHome = tool 'sonar-scanner'
    withSonarQubeEnv(credentialsId: 'SONARQUBE-TOKEN', installationName: 'sonar-scanner') {
        sh """
            ${scannerHome}/bin/sonar-scanner \\
                -Dsonar.projectName="${projectName}" \\
                -Dsonar.projectKey=${projectKey} \\
                -Dsonar.projectVersion=${projectVersion}
        """
    }
}
