FROM openjdk:17-ea-slim
VOLUME /tmp
COPY build/libs/discovery-service-0.1.jar DiscoveryService.jar
ENTRYPOINT ["java", "-jar", "DiscoveryService.jar"]
