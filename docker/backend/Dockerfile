FROM gradle:jdk21
WORKDIR /app

COPY backend/src src
COPY backend/build.gradle.kts .
COPY backend/settings.gradle.kts .
COPY backend/gradle/libs.versions.toml gradle/libs.versions.toml

# Build del progetto
RUN gradle build -x test --no-daemon --stacktrace --info

# Profilo backend e mysql di default
VOLUME ["/app/data"]

ARG SPRING_PROFILE=backend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/build/libs/E4-Collab-1.0-SNAPSHOT.jar"]