FROM openjdk:17-ea-slim
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks
COPY build/libs/config-service-0.1.jar ConfigService.jar
ENTRYPOINT ["java", "-jar", "ConfigService.jar"]
