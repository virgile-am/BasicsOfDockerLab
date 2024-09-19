# Use JDK 21 slim version as the base image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged War file into the container
COPY target/BasicsOfDockerLab-0.0.1-SNAPSHOT.jar /app/BasicsOfDockerLab-0.0.1-SNAPSHOT.jar

# Expose the application port
EXPOSE 4000

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "BasicsOfDockerLab-0.0.1-SNAPSHOT.jar"]
