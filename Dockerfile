FROM openjdk:21-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Загружаем зависимости (этот слой будет кэшироваться)
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/UserRegistrationSpringSecurity-0.0.1-SNAPSHOT.jar"]
