# STAGE 1: BUILD
FROM gradle:8.0-jdk17 AS builder
WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

# jar
RUN ./gradlew clean bootJar -x test --no-daemon

# STAGE 2: RUNTIME
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Profilo frontend e mysql
ARG SPRING_PROFILE=frontend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
