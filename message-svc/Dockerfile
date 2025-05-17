#build
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /app

# Copy shared-lib
COPY ./shared-lib /app/shared-lib

# Build shared-lib
WORKDIR /app/shared-lib
RUN mvn clean install -DskipTests

# Copy message-svc
WORKDIR /app
COPY ./message-svc /app/message-svc

# Build message-svc
WORKDIR /app/message-svc
RUN mvn clean package -DskipTests

#run
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=build /app/message-svc/target/*.jar /app/message-svc.jar

EXPOSE 8081

CMD ["java", "-jar", "message-svc.jar"]