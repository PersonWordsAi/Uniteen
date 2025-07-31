# Boshlanish uchun Maven bilan JDK image
FROM maven:3.8.5-openjdk-17 AS build

# Loyihani konteynerga nusxalash
COPY . /app
WORKDIR /app

# Loyihani build qilish
RUN mvn clean package -DskipTests

# Yengil JDK bilan run image
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar /uniteenbot.jar

# Botni ishga tushurish
ENTRYPOINT ["java", "-jar", "/uniteenbot.jar"]