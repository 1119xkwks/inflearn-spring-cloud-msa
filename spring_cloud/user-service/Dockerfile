FROM openjdk:17-ea-slim
VOLUME /tmp
COPY build/libs/user-service-0.1.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]
