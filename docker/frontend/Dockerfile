FROM gradle:jdk17 AS e4collab-backend-build
WORKDIR /app

COPY src src
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Build del progetto
RUN gradle build -x test --no-daemon --stacktrace --info

# Profilo frontend e mysql
ARG SPRING_PROFILE=frontend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8080

#VOLUME ["/app/data"]
#ENTRYPOINT ["sleep", "infinity"]
ENTRYPOINT ["java", "-jar", "/app/build/libs/E4-Collab-1.0-SNAPSHOT.jar"]
