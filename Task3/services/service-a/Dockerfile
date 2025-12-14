# Этап 1: Сборка с помощью Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и собираем зависимости (для кэширования)
COPY pom.xml .
COPY src ./src

# Собираем JAR
RUN mvn clean package

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=build /app/target/service-a-1.0-SNAPSHOT.jar ./app.jar

# Открываем порт (если сервис слушает 8080, как в service-b)
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]
