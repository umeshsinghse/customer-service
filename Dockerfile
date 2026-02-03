# Updated Dockerfile with current JDK 17 image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/*.jar customer-service.jar

# Expose port
EXPOSE 8080

# Run the application with JVM optimizations
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-XX:+UseG1GC","-XX:MaxGCPauseMillis=200","-Djava.security.egd=file:/dev/./urandom","-jar","/app/customer-service.jar"]