FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn --batch-mode clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/market-data-replay-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-cp", "app.jar", "com.hrutvik.marketdata.MarketDataApplication"]
