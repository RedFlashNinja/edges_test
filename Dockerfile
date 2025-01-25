# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

ARG JAR_FILE=target/edges_test-1.0-SNAPSHOT.jar
ARG PROP_FILE=src/main/resources/application.yaml

# Copy the JAR file into the container
COPY ${JAR_FILE} app.jar
COPY ${PROP_FILE} /app/application.properties

# Expose the port your Spring Boot app runs on
EXPOSE 9080
EXPOSE 9082
EXPOSE 8080

# Set the default command to run your app using java -jar
ENTRYPOINT ["java", "-jar", "app.jar"]
