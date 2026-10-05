#build
FROM maven:3.10.0-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy shared-lib
COPY ./shared-lib /app/shared-lib

# Build shared-lib
WORKDIR /app/shared-lib
RUN mvn clean install -DskipTests

# Copy user-svc
WORKDIR /app
COPY ./user-svc /app/user-svc

# Build user-svc
WORKDIR /app/user-svc
RUN mvn clean package -DskipTests

#run
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/user-svc/target/*.jar /app/user-svc.jar

EXPOSE 8081

CMD ["java", "-jar", "user-svc.jar"]