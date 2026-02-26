FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/loan-management-system-*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]