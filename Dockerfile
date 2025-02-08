FROM openjdk:17
ARG JAR_FILE=target/*.jar
ADD ${JAR_FILE} OAuthService.jar
ENTRYPOINT ["java","-jar","OAuthService.jar"]

