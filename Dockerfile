FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/lucida-account.jar
ADD ${JAR_FILE} lucida-account.jar
ENTRYPOINT ["java","-jar","/lucida-account.jar"]