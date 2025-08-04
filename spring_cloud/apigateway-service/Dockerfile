FROM openjdk:17-ea-slim
VOLUME /tmp
COPY build/libs/apigateway-service-0.1.jar ApigatewayService.jar
ENTRYPOINT ["java", "-jar", "ApigatewayService.jar"]
