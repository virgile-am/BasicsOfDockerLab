# Basics of Docker Lab

## Project Overview

This project demonstrates the core principles of Docker by creating a simple Spring Boot application, containerizing it with Docker, and managing it using Docker Compose. The setup includes a PostgreSQL database and a Spring Boot application that interacts with it.

## Lab Objectives

- Understand core DevOps principles and practices.
- Build and run Docker images.
- Create Dockerfiles for application containerization.
- Define and run multi-container applications using Docker Compose.

## Project Structure

- **src/**: Contains the source code for the Spring Boot application.
- **Dockerfile**: Dockerfile for containerizing the Spring Boot application.
- **docker-compose.yml**: Docker Compose configuration file to set up the multi-container environment.
- **.env**: Environment variables file used to configure the application and database.
- **application.properties**: Configuration file for the Spring Boot application.

## Dockerfile

The Dockerfile creates an image for the Spring Boot application.

```dockerfile
# Use the official OpenJDK 21 base image
FROM openjdk:21-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/myapp.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose port 8080
EXPOSE 8080
