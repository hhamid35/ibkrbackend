# Use a base image that includes Maven and OpenJDK
FROM maven:3.8.4-openjdk-17-slim

# Set the working directory
WORKDIR /ibkrbackend

# Copy the source code from GitHub into the Docker image
COPY . /ibkrbackend

# Install TwsApi.jar
RUN mvn install:install-file -Dfile=./lib/TwsApi.jar -DgroupId=com.ib -DartifactId=TwsApi -Dversion=1.0 -Dpackaging=jar

# Build with Maven
RUN mvn -B package --file pom.xml

# Publish to GitHub Packages Apache Maven
#RUN mvn deploy -s $GITHUB_WORKSPACE/settings.xml

# Expose the necessary port for the application
EXPOSE $SERVER_PORT

# Start the application
CMD ["java", "-jar", "/ibkrbackend/target/*.jar"]