FROM openjdk:17-jdk-slim as builder
WORKDIR /opt/softwareag/

ARG JAR_FILE=build/libs/application-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:17-jdk-slim
WORKDIR /opt/softwareag/

USER root

COPY --from=builder /opt/softwareag/dependencies/ ./
COPY --from=builder /opt/softwareag/spring-boot-loader/ ./
COPY --from=builder /opt/softwareag/snapshot-dependencies/ ./
COPY --from=builder /opt/softwareag/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]