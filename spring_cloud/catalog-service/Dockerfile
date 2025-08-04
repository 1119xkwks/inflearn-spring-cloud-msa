FROM openjdk:17-ea-slim
VOLUME /tmp
COPY build/libs/catalog-service-0.1.jar CatalogService.jar
ENTRYPOINT ["java", "-jar", "CatalogService.jar"]
