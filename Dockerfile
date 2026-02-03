# Dockerfile::Dockerfile::C:/customer-service/Dockerfile
# Updated Dockerfile with current JDK 17 image
FROM eclipse-temurin:17-jdk

# Copy the JAR file
COPY target/*.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","/app.jar"]