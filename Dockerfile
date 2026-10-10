FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src/ src/

RUN ./gradlew bootJar --no-daemon -x test && \
    cp $(find build/libs -name "*.jar" ! -name "*-plain.jar") app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
