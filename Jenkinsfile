  pipeline {
      agent any

      environment {
          DOCKER_IMAGE = 'vg1k2/basicsofdockerlab:v1.0'
          CONTAINER_NAME = 'basicsofdockerlab'
          SPRING_PORT = '4000'
          POSTGRES_DB = 'productdb'
          POSTGRES_USER = 'postgres'
          POSTGRES_PASSWORD = '1010'
          // AWS credentials for Terraform
          AWS_ACCESS_KEY_ID = credentials('aws-access-key-id')
          AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
          AWS_KEY_PAIR_NAME = credentials('aws-key-pair-name')
          // Define SSH key path
          SSH_KEY_PATH = 'C:\\ProgramData\\Jenkins\\.ssh\\ec2-ssh-key'
      }

      stages {
          stage('Checkout Code') {
              steps {
                  git branch: 'master', url: 'https://github.com/virgile-am/BasicsOfDockerLab.git'
              }
          }

          stage('Setup SSH Key') {
              steps {
                  script {
                      // Create .ssh directory with explicit path for Jenkins user
                      bat '''
                          if not exist "C:\\ProgramData\\Jenkins\\.ssh" (
                              mkdir "C:\\ProgramData\\Jenkins\\.ssh"
                          )
                      '''

                      withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key',
                                                        keyFileVariable: 'SSH_KEY')]) {
                          bat '''
                              copy "%SSH_KEY%" "C:\\ProgramData\\Jenkins\\.ssh\\ec2-ssh-key"
                              icacls "C:\\ProgramData\\Jenkins\\.ssh\\ec2-ssh-key" /inheritance:r
                              icacls "C:\\ProgramData\\Jenkins\\.ssh\\ec2-ssh-key" /grant:r "NT AUTHORITY\\SYSTEM":R
                          '''
                      }
                  }
              }
          }

          stage('Create Terraform Config') {
              steps {
                  script {
                      writeFile file: 'main.tf', text: '''
                          # Configure AWS Provider and Variables
                          provider "aws" {
                              region     = "us-west-2"
                              access_key = var.aws_access_key
                              secret_key = var.aws_secret_key
                          }

                          variable "aws_access_key" {
                              description = "AWS Access Key"
                              type        = string
                          }

                          variable "aws_secret_key" {
                              description = "AWS Secret Key"
                              type        = string
                          }

                          variable "key_pair_name" {
                              description = "Name of the AWS key pair"
                              type        = string
                          }

                          # Get latest Amazon Linux 2 AMI
                          data "aws_ami" "amazon_linux_2" {
                              most_recent = true
                              owners      = ["amazon"]

                              filter {
                                  name   = "name"
                                  values = ["amzn2-ami-hvm-*-x86_64-gp2"]
                              }
                          }

                          # Create Security Group
                          resource "aws_security_group" "app_sg" {
                              name        = "docker-app-sg"
                              description = "Security group for Docker application"

                              ingress {
                                  from_port   = 22
                                  to_port     = 22
                                  protocol    = "tcp"
                                  cidr_blocks = ["0.0.0.0/0"]
                                  description = "SSH access"
                              }

                              ingress {
                                  from_port   = 80
                                  to_port     = 80
                                  protocol    = "tcp"
                                  cidr_blocks = ["0.0.0.0/0"]
                                  description = "HTTP access"
                              }

                              ingress {
                                  from_port   = 4000
                                  to_port     = 4000
                                  protocol    = "tcp"
                                  cidr_blocks = ["0.0.0.0/0"]
                                  description = "Application port"
                              }

                              egress {
                                  from_port   = 0
                                  to_port     = 0
                                  protocol    = "-1"
                                  cidr_blocks = ["0.0.0.0/0"]
                              }

                              tags = {
                                  Name = "docker-app-sg"
                              }
                          }

                          # Create EC2 Instance
                          resource "aws_instance" "app_server" {
                              ami           = data.aws_ami.amazon_linux_2.id
                              instance_type = "t2.micro"
                              key_name      = var.key_pair_name

                              vpc_security_group_ids = [aws_security_group.app_sg.id]

                              user_data = <<-EOF
                                          #!/bin/bash
                                          sudo yum update -y
                                          sudo amazon-linux-extras install docker -y
                                          sudo service docker start
                                          sudo usermod -a -G docker ec2-user
                                          sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                                          sudo chmod +x /usr/local/bin/docker-compose
                                          EOF

                              tags = {
                                  Name = "DockerAppServer"
                              }
                          }

                          # Output the public IP
                          output "ec2_public_ip" {
                              value = aws_instance.app_server.public_ip
                          }
                      '''
                  }
              }
          }

          stage('Terraform Init') {
              steps {
                  script {
                      echo 'Initializing Terraform...'
                      bat 'terraform init'
                  }
              }
          }

          stage('Terraform Plan') {
              steps {
                  script {
                      echo 'Planning Terraform infrastructure...'
                      bat '''
                          terraform plan ^
                          -var="aws_access_key=%AWS_ACCESS_KEY_ID%" ^
                          -var="aws_secret_key=%AWS_SECRET_ACCESS_KEY%" ^
                          -var="key_pair_name=%AWS_KEY_PAIR_NAME%" ^
                          -out=tfplan
                      '''
                  }
              }
          }

          stage('Terraform Apply') {
              steps {
                  script {
                      echo 'Applying Terraform infrastructure...'
                      bat 'terraform apply -auto-approve tfplan'
                  }
              }
          }

          stage('Read EC2 IP') {
              steps {
                  script {
                      def tfOutput = bat(script: 'terraform output -raw ec2_public_ip', returnStdout: true).trim()
                      env.EC2_IP = tfOutput
                      echo "EC2 Instance IP: ${env.EC2_IP}"
                  }
              }
          }

          stage('Build with Maven') {
              steps {
                  script {
                      echo 'Building the application with Maven...'
                      bat 'mvn clean package -DskipTests'
                  }
              }
          }

          stage('Run Tests') {
              steps {
                  script {
                      echo 'Running unit tests...'
                      bat 'mvn test'
                  }
              }
          }

          stage('Build Docker Image') {
              steps {
                  script {
                      echo 'Building Docker image...'
                      bat "docker build -t ${DOCKER_IMAGE} ."
                  }
              }
          }

          stage('Push Docker Image to Docker Hub') {
              steps {
                  script {
                      echo 'Pushing Docker image to Docker Hub...'
                      withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                          bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                          bat "docker push ${DOCKER_IMAGE}"
                          bat "docker logout"
                      }
                  }
              }
          }

          stage('Deploy Docker Container to EC2') {
              steps {
                  script {
                      echo 'Deploying Docker container on the EC2 instance...'
                      bat """
                          ssh -i "${env.SSH_KEY_PATH}" -o StrictHostKeyChecking=no ec2-user@%EC2_IP% "
                          docker pull ${DOCKER_IMAGE} &&
                          docker-compose down &&
                          docker-compose up -d
                          "
                      """
                  }
              }
          }
      }

      post {
          always {
              echo 'Cleaning workspace...'
              cleanWs()
              bat "if exist \"${env.SSH_KEY_PATH}\" del \"${env.SSH_KEY_PATH}\""
          }
          success {
              echo 'Pipeline succeeded! The application has been built, tested, and deployed on EC2.'
          }
          failure {
              echo 'Pipeline failed. Please check the console output for issues.'
          }
      }
  }