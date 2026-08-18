# Eén Dockerfile voor alle vier de services. De module wordt meegegeven als
# build argument, zodat de Maven-reactor maar één keer gebouwd hoeft te worden.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY entitlement-service/pom.xml entitlement-service/pom.xml
COPY playback-service/pom.xml playback-service/pom.xml
COPY edge-gateway/pom.xml edge-gateway/pom.xml
RUN mvn -B -q dependency:go-offline

COPY common/src common/src
COPY catalog-service/src catalog-service/src
COPY entitlement-service/src entitlement-service/src
COPY playback-service/src playback-service/src
COPY edge-gateway/src edge-gateway/src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app && mkdir -p /artifacts/logs && chown -R app:app /artifacts
COPY --from=build /build/${SERVICE}/target/${SERVICE}-1.0.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
