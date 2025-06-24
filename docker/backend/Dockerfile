FROM gradle:jdk17
WORKDIR /app

COPY src src
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Build del progetto
RUN gradle build -x test --no-daemon --stacktrace --info

# Profilo backend e mysql di default
VOLUME ["/app/data"]

ARG SPRING_PROFILE=backend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/build/libs/E4-Collab-1.0-SNAPSHOT.jar"]