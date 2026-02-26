FROM eclipse-temurin:17-jdk AS builder
WORKDIR /builder

COPY target/loan-management-system-*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /builder/dependencies/ ./
COPY --from=builder /builder/snapshot-dependencies/ ./
COPY --from=builder /builder/spring-boot-loader/ ./
COPY --from=builder /builder/application/ ./

ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]