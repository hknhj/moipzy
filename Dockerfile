# Docker file

# jdk 17
FROM openjdk:17-jdk

# arg
ARG JAR_FILE=build/libs/*.jar

# jar file copy
COPY ${JAR_FILE} moipzy-server.jar

ENTRYPOINT ["java", "-jar", "moipzy-server.jar"]