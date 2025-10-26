
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -u 10001 -r -s /sbin/nologin appuser

COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 7070

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
