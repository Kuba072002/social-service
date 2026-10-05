#build
FROM maven:3.10.0-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy shared-lib
COPY ./shared-lib /app/shared-lib

# Build shared-lib
WORKDIR /app/shared-lib
RUN mvn clean install -DskipTests

# Copy chat-svc
WORKDIR /app
COPY ./chat-svc /app/chat-svc

# Build chat-svc
WORKDIR /app/chat-svc
RUN mvn clean package -DskipTests

#run
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/chat-svc/target/*.jar /app/chat-svc.jar

EXPOSE 8081

CMD ["java", "-jar", "chat-svc.jar"]