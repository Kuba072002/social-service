#build
FROM maven:3.10.0-eclipse-temurin-25-alpine AS build

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
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/message-svc/target/*.jar /app/message-svc.jar

EXPOSE 8081

CMD ["java", "-jar", "message-svc.jar"]