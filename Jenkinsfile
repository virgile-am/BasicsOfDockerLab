 pipeline {
     agent any

     environment {
         DOCKER_IMAGE = 'vg1k2/basicsofdockerlab:v1.0'
         CONTAINER_NAME = 'basicsofdockerlab'
         SPRING_PORT = '4000'
         POSTGRES_DB = 'productdb'
         POSTGRES_USER = 'postgres'
         POSTGRES_PASSWORD = '1010'
     }

     stages {
         stage('Checkout') {
             steps {
                 git branch: 'master', url: 'https://github.com/virgile-am/BasicsOfDockerLab.git'
             }
         }

         stage('Build with Maven') {
             steps {
                 script {
                     echo 'Building with Maven...'
                     bat 'mvn clean package -DskipTests'
                 }
             }
         }

         stage('Run Tests') {
             steps {
                 script {
                     echo 'Running tests...'
                     bat 'mvn test'
                 }
             }
         }

         stage('Build Docker Image') {
             steps {
                 script {
                     echo 'Building Docker image...'
                     bat "docker build -t %DOCKER_IMAGE% ."
                 }
             }
         }

         stage('Push Docker Image') {
             steps {
                 script {
                     echo 'Pushing Docker image to Docker Hub...'
                     withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                         bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                         bat "docker push %DOCKER_IMAGE%"
                         bat "docker logout"
                     }
                 }
             }
         }

         stage('Deploy with Docker Compose') {
             steps {
                 script {
                     echo 'Deploying with Docker Compose...'
                     withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                         bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"

                         withEnv([
                             "DOCKER_IMAGE=${DOCKER_IMAGE}",
                             "SPRING_PORT=${SPRING_PORT}",
                             "POSTGRES_DB=${POSTGRES_DB}",
                             "POSTGRES_USER=${POSTGRES_USER}",
                             "POSTGRES_PASSWORD=${POSTGRES_PASSWORD}"
                         ]) {
                             bat "docker-compose down"
                             bat "docker-compose build app"
                             bat "docker-compose up -d"
                         }

                         bat "docker logout"
                     }
                 }
             }
         }
     }

     post {
         always {
             echo 'Cleaning workspace...'
             cleanWs()
         }
         success {
             echo 'Pipeline succeeded! The application has been built, tested, and deployed with Docker Compose.'
         }
         failure {
             echo 'Pipeline failed. Please check the console output to fix the issue.'
         }
     }
 }