FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar bank_rest_app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "bank_rest_app.jar"]