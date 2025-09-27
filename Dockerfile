# Stage 1: Build the application
# Use a specific Maven version with a specific JDK for reproducibility
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching.
# Dependencies will only be re-downloaded if pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Create the final, smaller runtime image
# Use a JRE image which is smaller than a full JDK
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the application JAR from the build stage.
# Be specific with the JAR name to avoid ambiguity.
COPY --from=build /app/target/multi_tanent-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Define the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]