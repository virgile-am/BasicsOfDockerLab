 pipeline {
     agent any

     environment {
         DOCKER_IMAGE = 'vg1k2/basicsofdockerlab:v1.0'
         CONTAINER_NAME = 'basicsofdockerlab'
         SPRING_PORT = '4000'
         REMOTE_HOST = 'user@your-remote-docker-host'
         REMOTE_DOCKER_SOCKET = '/var/run/docker.sock'
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

         stage('Deploy to Remote Docker') {
             steps {
                 script {
                     echo 'Deploying to remote Docker host...'
                     withCredentials([sshUserPrivateKey(credentialsId: 'remote-docker-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                         bat """
                         ssh -i %SSH_KEY% -o StrictHostKeyChecking=no %REMOTE_HOST% "
                             docker pull %DOCKER_IMAGE% &&
                             docker stop %CONTAINER_NAME% || true &&
                             docker rm %CONTAINER_NAME% || true &&
                             docker run -d --name %CONTAINER_NAME% -p %SPRING_PORT%:4000 \
                             -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-render-postgres-host:5432/your-db-name \
                             -e SPRING_DATASOURCE_USERNAME=your-username \
                             -e SPRING_DATASOURCE_PASSWORD=your-password \
                             %DOCKER_IMAGE%
                         "
                         """
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
             echo 'Pipeline succeeded! The application has been built, tested, and deployed to the remote Docker host.'
         }
         failure {
             echo 'Pipeline failed. Please check the console output to fix the issue.'
         }
     }
 }