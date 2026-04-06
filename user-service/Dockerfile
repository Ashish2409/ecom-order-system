FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY build.gradle .
COPY src ./src
COPY gradlew .
COPY gradle ./gradle
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]