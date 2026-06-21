FROM gradle:8.8-jdk21 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21.0.2_13-jre-jammy

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
