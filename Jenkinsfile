 pipeline {
     agent any

     environment {
         DOCKER_IMAGE = 'vg1k2/basicsofdockerlab:v1.0'
         SPRING_PORT = '4000'
         # PostgreSQL Database environment variables
         DB_URL = 'jdbc:postgresql://dpg-crl725btq21c73e97u8g-a.oregon-postgres.render.com/jenkinsdatabase'
         DB_USER = 'jenkinsdatabase_user'
         DB_PASSWORD = 'NlQemNvYTRIfUS7cOhvSDgBM5Y53xNXY'
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
                     echo 'Pushing Docker image...'
                     withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                         bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                         bat "docker push %DOCKER_IMAGE%"
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
             echo 'Pipeline succeeded! The application has been built, tested, and Docker image pushed to DockerHub.'
         }
         failure {
             echo 'Pipeline failed. Please check the console output to fix the issue.'
         }
     }
 }
