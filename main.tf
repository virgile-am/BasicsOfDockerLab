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

# Check if the key pair already exists
data "aws_key_pair" "existing_key" {
  key_name = var.key_pair_name
}

# Check if the security group already exists
data "aws_security_group" "existing_app_sg" {
  filter {
    name   = "group-name"
    values = ["docker-app-sg"]
  }
}

# Create the security group if it doesn't exist
resource "aws_security_group" "app_sg" {
  count = length(data.aws_security_group.existing_app_sg.ids) == 0 ? 1 : 0
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
  ami                    = "ami-0992959aaea5762e8" // Replace with your preferred AMI
  instance_type         = "t2.micro"
  key_name              = var.key_pair_name

  vpc_security_group_ids = aws_security_group.app_sg[*].id

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
