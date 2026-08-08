pipeline {
    agent any
    
     tools{
        nodejs 'nodejs-20.0.0'
    }

    environment {
        DOCKER_IMAGE = 'chanserey/reactjs-app'
        CONTAINER_NAME = 'reactjs-app'
    }

    stages {

        stage('Clone Code') {
            steps {
                git 'https://github.com/ChansereyGit/reactjs-devop8-template.git'
            }
        }

        stage('Test') {
            steps {
                sh '''
                    echo "Running tests..."

                    npm ci
                    npm test -- --watchAll=false
                '''
            }
        }

        stage('Build') {
            steps {
                sh '''
                    echo "Building Docker image..."

                    docker build \
                        -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                        -t ${DOCKER_IMAGE}:latest \
                        -f prod.Dockerfile .
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials2',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKER_IMAGE}:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    echo "Stopping old container..."

                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    echo "Pulling latest image..."

                    docker pull ${DOCKER_IMAGE}:latest

                    echo "Starting new container..."

                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p 3000:80 \
                        ${DOCKER_IMAGE}:latest

                    docker ps
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}