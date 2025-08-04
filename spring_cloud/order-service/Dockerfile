FROM openjdk:17-ea-slim
VOLUME /tmp
COPY build/libs/order-service-0.1.jar OrderService.jar
ENTRYPOINT ["java", "-jar", "OrderService.jar"]
